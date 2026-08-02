package com.fabrica.conduit.electricity;

import com.fabrica.api.energy.CableTier;
import com.fabrica.api.energy.EnergyApiLookup;
import com.fabrica.api.energy.EnergyContainer;
import com.fabrica.api.energy.EnergyTier;
import com.fabrica.conduit.api.PipeEndpointType;
import com.fabrica.conduit.api.PipeNetworkNode;
import com.fabrica.conduit.impl.PipeBlockEntity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import static com.fabrica.conduit.api.PipeEndpointType.BLOCK;
import static com.fabrica.conduit.api.PipeEndpointType.PIPE;

/**
 * Узел электрической сети — один блок кабеля, входящий в сеть. Отвечает за:
 * - список соединений (connections): по каким направлениям кабель подключён
 *   к машинам/хранилищам энергии (EnergyContainer) — подключение к другим
 *   трубам хранит менеджер сети, а не узел;
 * - поиск соседних хранилищ через EnergyApiLookup с учётом стороны блока;
 * - запас энергии (eu): каждый узел хранит свою долю энергии сети
 *   (равномерно распределяемую сетью каждый тик);
 * - авто-подключение к машинам при установке (buildInitialConnections),
 *   отслеживание появления/исчезновения машин (updateConnections),
 *   ручное подключение игроком (addConnection) и сохранение/чтение состояния.
 */
public class ElectricityNetworkNode extends PipeNetworkNode {
	// Список направлений, по которым кабель подключён к машинам/хранилищам.
	private List<Direction> connections = new ArrayList<>();
	// Запас энергии данного узла (доля сети, EU). Перезаписывается сетью каждый тик.
	long eu = 0;

	/**
	 * Собирает все подключённые хранилища энергии в общий список сети.
	 * Для каждого направления из connections ищет EnergyContainer соседнего
	 * блока (со стороны, обращённой к кабелю) и, если он совместим с тиром
	 * кабеля, добавляет его в список storages — из него сеть будет
	 * извлекать/вставлять энергию в этом тике.
	 */
	public void appendAttributes(ServerLevel world, BlockPos pos, CableTier cableTier, List<EnergyContainer> storages) {
		for (Direction direction : connections) {
			EnergyContainer storage = EnergyApiLookup.CONTAINER.find(world, pos.relative(direction), direction.getOpposite());
			if (storage == null || !ElectricityNetwork.canConnect(cableTier, storage)) {
				continue;
			}
			storages.add(storage);
		}
	}

	// Первичное авто-подключение при установке трубы: подключаемся ко всем
	// сторонам, где рядом есть подходящее хранилище энергии.
	@Override
	public void buildInitialConnections(Level world, BlockPos pos) {
		for (Direction direction : Direction.values()) {
			if (canConnect(world, pos, direction)) {
				connections.add(direction);
			}
		}
	}

	/**
	 * Обновление соединений при изменении соседей (установке/удалении блоков).
	 * Кабель НЕ подключается к машинам автоматически позже (в отличие от
	 * жидкостных/предметных труб), поэтому здесь только удаляются соединения,
	 * ставшие недоступными (машина убрана или хранилище исчезло).
	 */
	@Override
	public void updateConnections(Level world, BlockPos pos) {
		// We don't connect by default, so we just have to remove connections that have
		// become unavailable
		for (int i = 0; i < connections.size();) {
			if (canConnect(world, pos, connections.get(i))) {
				i++;
			} else {
				connections.remove(i);
			}
		}
	}

	// Возвращает массив типов концов трубы для рендера моделей: PIPE — соединение
	// с соседней трубой (из менеджера сети), BLOCK — соединение с машиной/хранилищем.
	@Override
	public @Nullable PipeEndpointType[] getConnections(BlockPos pos) {
		PipeEndpointType[] connections = new PipeEndpointType[6];
		for (Direction direction : network.manager.getNodeLinks(pos)) {
			connections[direction.get3DDataValue()] = PIPE;
		}
		for (Direction connection : this.connections) {
			connections[connection.get3DDataValue()] = BLOCK;
		}
		return connections;
	}

	// Удаляет соединение по направлению (например, когда игрок сломал машину
	// или отсоединил кабель инструментом). Если соединения нет — просто ничего не делает.
	@Override
	public void removeConnection(Level world, BlockPos pos, Direction direction) {
		// Remove if it exists
		for (int i = 0; i < connections.size(); i++) {
			if (connections.get(i) == direction) {
				connections.remove(i);
				return;
			}
		}
	}

	// Ручное подключение игроком: если соединения ещё нет и рядом есть подходящее
	// хранилище — добавляем направление в список.
	@Override
	public void addConnection(PipeBlockEntity pipe, Player player, Level world, BlockPos pos, Direction direction) {
		// Refuse if it already exists
		for (Direction connection : connections) {
			if (connection == direction) {
				return;
			}
		}
		// Otherwise try to connect
		if (canConnect(world, pos, direction)) {
			connections.add(direction);
		}
	}

	// Сериализация: сохраняет список соединений в виде битовой маски
	// (бит 0-5 = направления) и запас энергии eu.
	@Override
	public void save(ValueOutput output) {
		int mask = 0;
		for (Direction connection : connections) {
			mask |= 1 << connection.get3DDataValue();
		}
		output.putInt("connections", mask);
		output.putLong("eu", eu);
	}

	// Десериализация: восстанавливает соединения из битовой маски и запас eu.
	@Override
	public void read(ValueInput input) {
		connections = new ArrayList<>();
		int mask = input.getIntOr("connections", 0);
		for (Direction direction : Direction.values()) {
			if ((mask & (1 << direction.get3DDataValue())) != 0) {
				connections.add(direction);
			}
		}
		eu = input.getLongOr("eu", 0);
	}

	// Проверка: есть ли рядом по направлению подходящее хранилище энергии,
	// совместимое с тиром кабеля данной сети.
	private boolean canConnect(Level world, BlockPos pos, Direction direction) {
		var storage = EnergyApiLookup.CONTAINER.find(world, pos.relative(direction), direction.getOpposite());
		return storage != null && ElectricityNetwork.canConnect(((ElectricityNetwork) network).tier, storage);
	}

	// Максимальная скорость передачи кабеля (EU/тик) — берётся из тира сети.
	public long getMaxTransfer() {
		return ((ElectricityNetwork) network).tier.maxTransfer();
	}
}
