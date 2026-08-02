package com.fabrica.conduit.fluid;

import com.fabrica.conduit.api.PipeEndpointType;
import com.fabrica.conduit.api.PipeNetworkNode;
import com.fabrica.conduit.api.PipeNetworkType;
import com.fabrica.conduit.impl.PipeBlockEntity;
import com.fabrica.conduit.impl.PipeNetworks;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import static com.fabrica.conduit.api.PipeEndpointType.BLOCK_IN;
import static com.fabrica.conduit.api.PipeEndpointType.BLOCK_IN_OUT;
import static com.fabrica.conduit.api.PipeEndpointType.BLOCK_OUT;
import static com.fabrica.conduit.api.PipeEndpointType.PIPE;

/**
 * Узел жидкостной сети — один блок жидкостной трубы. Отвечает за:
 * - список подключений FluidConnection: направление, режим (BLOCK_IN —
 *   вставка в трубу/IN_OUT — оба направления/OUT — извлечение) и приоритет;
 * - запас жидкости узла (amount): каждая труба хранит свою долю жидкости сети;
 * - авто-подключение к ёмкостям рядом: при установке трубы (buildInitialConnections)
 *   и позже — при появлении новых ёмкостей рядом (updateConnections, как в MI);
 * - выбор жидкости сети: если в данных сети жидкость ещё не выбрана, узел
 *   находит первую непустую ёмкость среди своих подключений;
 * - сбор целей (FluidTarget) для сети каждый тик, переключение режима
 *   IN/IN_OUT/OUT игроком и сериализацию состояния.
 */
public class FluidNetworkNode extends PipeNetworkNode {
	// Запас жидкости данного узла (мБ). Распределяется сетью поровну каждый тик.
	long amount = 0;
	// Все подключения к соседним блокам: направление, режим IN/IN_OUT/OUT, приоритет.
	private final List<FluidConnection> connections = new ArrayList<>();
	// Кэш типа жидкости, который узел показывал клиенту последний раз
	// (для синхронизации при смене жидкости).
	private FluidVariant cachedFluid = FluidVariant.blank();

	/**
	 * Собирает цели передачи для сети и выбирает жидкость, если она ещё не выбрана.
	 * Алгоритм:
	 * 1. Очистка некорректного запаса: amount не может превышать ёмкость узла,
	 *    а при пустой жидкости сети запас принудительно обнуляется.
	 * 2. Для каждого подключения ищется соседнее хранилище жидкости.
	 * 3. Если жидкость сети ещё не выбрана и подключение разрешает извлечение
	 *    (OUT/IN_OUT) — пробуем найти первую непустую ёмкость и записать её
	 *    жидкость в данные сети (network.data).
	 * 4. Каждое подключение добавляется в общий список targets как FluidTarget
	 *    с приоритетом и флагами canInsert/canExtract — сеть по нему передаёт.
	 */
	void gatherTargetsAndPickFluid(ServerLevel world, BlockPos pos, List<FluidTarget> targets) {
		FluidNetworkData data = (FluidNetworkData) network.data;
		FluidNetwork network = (FluidNetwork) this.network;

		// Запас узла не может превышать его ёмкость — ограничиваем.
		if (amount > network.nodeCapacity) {
			amount = network.nodeCapacity;
		}
		// Если жидкости в сети нет — запас узла не имеет смысла, обнуляем.
		if (amount > 0 && data.fluid().isBlank()) {
			amount = 0;
		}

		for (FluidConnection connection : connections) {
			var storage = getNeighborStorage(world, pos, connection);
			if (data.fluid().isBlank() && connection.canExtract()) {
				// Try to set fluid, will return null if none could be found.
				// Выбор жидкости: первая непустая ёмкость определяет тип жидкости сети.
				for (var view : storage.nonEmptyViews()) {
					if (view.getAmount() > 0) {
						network.data = data = new FluidNetworkData(view.getResource());
						break;
					}
				}
			}
			targets.add(new FluidTarget(connection.priority, storage, connection.canExtract(), connection.canInsert()));
		}
	}

	// Возвращает соседнее хранилище жидкости по направлению подключения
	// (сторона соседнего блока, обращённая к трубе). Если хранилища нет — пустое.
	@SuppressWarnings("unchecked")
	private Storage<FluidVariant> getNeighborStorage(ServerLevel world, BlockPos pos, FluidConnection connection) {
		Storage<FluidVariant> storage = FluidStorage.SIDED.find(world, pos.relative(connection.direction), connection.direction.getOpposite());
		return storage != null ? storage : Storage.empty();
	}

	// Первичное авто-подключение при установке трубы: ко всем сторонам, где рядом
	// есть ёмкость, создаётся подключение по умолчанию (IN_OUT, приоритет 0).
	@Override
	public void buildInitialConnections(Level world, BlockPos pos) {
		for (Direction direction : Direction.values()) {
			if (canConnect(world, pos, direction)) {
				connections.add(new FluidConnection(direction, BLOCK_IN_OUT, 0));
			}
		}
	}

	/**
	 * Обновление подключений при изменении соседей (вызывается при установке
	 * или удалении блоков рядом с трубой). Два шага:
	 * 1. Удаление подключений к блокам, с которыми теперь соединена ДРУГАЯ труба
	 *    (проверяются все типы сетей через PipeNetworks) — чтобы труба не
	 *    "смотрела" сквозь соседнюю трубу на ёмкость за ней.
	 * 2. Авто-подключение к вновь появившимся ёмкостям (как в Modern Industrialization):
	 *    если по направлению ещё нет подключения и рядом есть ёмкость — создаём его.
	 */
	@Override
	public void updateConnections(Level world, BlockPos pos) {
		// Remove the connection to the outside world if a connection to another pipe is made.
		var levelNetworks = PipeNetworks.get((ServerLevel) world);
		connections.removeIf(connection -> {
			for (var type : PipeNetworkType.getTypes().values()) {
				var manager = levelNetworks.getOptionalManager(type);
				if (manager != null && manager.hasLink(pos, connection.direction)) {
					return true;
				}
			}
			return false;
		});
		// Auto-connect to newly placed tanks, like MI
		for (Direction direction : Direction.values()) {
			boolean connected = connections.stream().anyMatch(connection -> connection.direction == direction);
			if (!connected && canConnect(world, pos, direction)) {
				connections.add(new FluidConnection(direction, BLOCK_IN_OUT, 0));
			}
		}
	}

	// Возвращает массив типов концов трубы для рендера: PIPE — соединение с трубой
	// (из менеджера сети), BLOCK_IN/BLOCK_IN_OUT/BLOCK_OUT — соединение с ёмкостью
	// и её режимом (нужно для отрисовки стрелок/режима в GUI и модели).
	@Override
	public @Nullable PipeEndpointType[] getConnections(BlockPos pos) {
		PipeEndpointType[] connections = new PipeEndpointType[6];
		for (Direction direction : network.manager.getNodeLinks(pos)) {
			connections[direction.get3DDataValue()] = PIPE;
		}
		for (FluidConnection connection : this.connections) {
			connections[connection.direction.get3DDataValue()] = connection.type;
		}
		return connections;
	}

	// Проверка: есть ли по направлению соседняя ёмкость, к которой можно подключиться.
	private boolean canConnect(Level world, BlockPos pos, Direction direction) {
		return FluidStorage.SIDED.find(world, pos.relative(direction), direction.getOpposite()) != null;
	}

	// Удаляет подключение по направлению (например, игрок отсоединил трубу инструментом).
	@Override
	public void removeConnection(Level world, BlockPos pos, Direction direction) {
		// Remove if it exists
		connections.removeIf(connection -> connection.direction == direction);
	}

	// Переключение режима подключения игроком (правый клик инструментом):
	// цикл IN (только вставка) -> IN_OUT (оба направления) -> OUT (только
	// извлечение) -> IN. Возвращает false, если подключения по направлению нет.
	@Override
	public boolean cycleConnectionMode(Level world, BlockPos pos, Direction direction) {
		// Cycle import -> import/export -> export -> import
		for (FluidConnection connection : connections) {
			if (connection.direction == direction) {
				if (connection.type == BLOCK_IN) {
					connection.type = BLOCK_IN_OUT;
				} else if (connection.type == BLOCK_IN_OUT) {
					connection.type = BLOCK_OUT;
				} else {
					connection.type = BLOCK_IN;
				}
				return true;
			}
		}
		return false;
	}

	// Ручное подключение игроком: если подключения ещё нет и рядом есть ёмкость —
	// создаём подключение по умолчанию (IN_OUT, приоритет 0).
	@Override
	public void addConnection(PipeBlockEntity pipe, Player player, Level world, BlockPos pos, Direction direction) {
		// Refuse if it already exists
		for (FluidConnection connection : connections) {
			if (connection.direction == direction) {
				return;
			}
		}
		// Otherwise try to connect
		if (canConnect(world, pos, direction)) {
			connections.add(new FluidConnection(direction, BLOCK_IN_OUT, 0));
		}
	}

	// Сериализация узла: запас жидкости (amount) и все подключения — каждое
	// как CompoundTag с закодированным типом (0/1/2) и приоритетом,
	// сохранённый под ключом-направлением ("north", "south", ...).
	@Override
	public void save(ValueOutput output) {
		output.putLong("amount", amount);
		for (FluidConnection connection : connections) {
			CompoundTag connectionTag = new CompoundTag();
			connectionTag.putByte("connections", (byte) encodeConnectionType(connection.type));
			connectionTag.putInt("priority", connection.priority);
			output.store(connection.direction.toString(), CompoundTag.CODEC, connectionTag);
		}
	}

	// Десериализация: восстанавливает запас жидкости и список подключений
	// из сохранённых данных (направление из ключа, тип и приоритет из тега).
	@Override
	public void read(ValueInput input) {
		amount = input.getLongOr("amount", 0);
		var keys = input.keySet();
		for (Direction direction : Direction.values()) {
			if (keys.contains(direction.toString())) {
				CompoundTag connectionTag = input.read(direction.toString(), CompoundTag.CODEC).orElseThrow();
				connections.add(new FluidConnection(direction, decodeConnectionType(connectionTag.getByteOr("connections", (byte) 0)),
						connectionTag.getIntOr("priority", 0)));
			}
		}
	}

	// Декодирование числа в режим подключения: 0 = IN, 1 = IN_OUT, 2 = OUT.
	private static PipeEndpointType decodeConnectionType(int i) {
		return i == 0 ? BLOCK_IN : i == 1 ? BLOCK_IN_OUT : BLOCK_OUT;
	}

	// Кодирование режима подключения в число: IN = 0, IN_OUT = 1, OUT = 2.
	private static int encodeConnectionType(PipeEndpointType connection) {
		return connection == BLOCK_IN ? 0 : connection == BLOCK_IN_OUT ? 1 : 2;
	}

	// Синхронизация клиенту: записывает текущую жидкость сети в NBT-тег
	// (используется при передаче данных узла на клиент для отрисовки жидкости).
	@Override
	public CompoundTag writeCustomData(HolderLookup.Provider registries) {
		CompoundTag tag = new CompoundTag();
		tag.store("fluid", FluidVariant.CODEC, ((FluidNetworkData) network.data).fluid());
		return tag;
	}

	// Вызывается сетью после каждого тика: если жидкость сети изменилась,
	// помечает блок как изменённый (эквивалент sync()) — клиент перерисует
	// трубу с новой жидкостью внутри.
	public void afterTick(ServerLevel world, BlockPos pos) {
		FluidVariant networkFluid = ((FluidNetworkData) network.data).fluid();
		if (!networkFluid.equals(cachedFluid)) {
			cachedFluid = networkFluid;
			// Equivalent to calling sync()
			world.getChunkSource().blockChanged(pos);
		}
	}

	/**
	 * Одно подключение трубы к соседнему блоку: направление, режим
	 * (IN = вставка в трубу, OUT = извлечение из трубы, IN_OUT = оба) и приоритет
	 * (чем больше — тем раньше эта цель получит/отдаст жидкость).
	 */
	private class FluidConnection {
		// Направление, в котором находится подключённый блок.
		private final Direction direction;
		// Режим подключения (IN / IN_OUT / OUT).
		private PipeEndpointType type;
		// Приоритет передачи (сортировка целей при передаче).
		private int priority;

		private FluidConnection(Direction direction, PipeEndpointType type, int priority) {
			this.direction = direction;
			this.type = type;
			this.priority = priority;
		}

		// Разрешена ли вставка жидкости в трубу через это подключение (IN/IN_OUT).
		private boolean canInsert() {
			return type == BLOCK_IN || type == BLOCK_IN_OUT;
		}

		// Разрешено ли извлечение жидкости из трубы через это подключение (OUT/IN_OUT).
		private boolean canExtract() {
			return type == BLOCK_OUT || type == BLOCK_IN_OUT;
		}
	}
}
