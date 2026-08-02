package com.fabrica.conduit.api;

import com.fabrica.conduit.impl.PipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Отвечает за узел сети — одну трубу в блоке (PipeBlockEntity): знает свою
 * сеть (network), вычисляет соединения (труба-труба, труба-машина) и их
 * режимы ввода/вывода, добавляет/удаляет соединения, сохраняется в NBT,
 * поставляет клиенту данные для рендера (writeCustomData) и предметы для
 * дропа. Конкретные типы узлов (предметный, жидкостный, электрический)
 * реализуют поведение своей сети.
 */
public abstract class PipeNetworkNode {
	@Nullable
	protected PipeNetwork network;

	// Пересчитывает соединения узла (автоматически ищет трубы и машины
	// вокруг). Вызывается при изменении соседей и после загрузки.
	public void updateConnections(Level world, BlockPos pos) {}

	// Строит начальные соединения сразу после установки трубы.
	public void buildInitialConnections(Level world, BlockPos pos) {}

	/**
	 * Get connections. Must return a size 6 array containing the 6 connections in
	 * the Direction order. Null can be used to render no connection.
	 */
	public abstract @Nullable PipeEndpointType[] getConnections(BlockPos pos);

	// Удаляет соединение на стороне direction (трубу или машину).
	public abstract void removeConnection(Level world, BlockPos pos, Direction direction);

	// Добавляет соединение на стороне direction: решает, подключать ли трубу
	// (через link) или машину, и обновляет режим ввода/вывода.
	public abstract void addConnection(PipeBlockEntity pipe, Player player, Level world, BlockPos pos, Direction direction);

	/**
	 * Cycle the import/export mode of the connection to a machine on the given
	 * side. Returns true if the mode was changed.
	 */
	public boolean cycleConnectionMode(Level world, BlockPos pos, Direction direction) {
		return false;
	}

	// Сериализация узла: save — на диск, read — чтение (вызываются до
	// привязки к миру, поэтому загрузка в сеть откладывается).
	public abstract void save(ValueOutput output);

	public abstract void read(ValueInput input);

	// Тип трубы и менеджер сетей берутся из текущей сети узла.
	public final PipeNetworkType getType() {
		return network.manager.getType();
	}

	public final PipeNetworkManager getManager() {
		return network.manager;
	}

	// Доп. данные для рендера клиента (например, количество жидкости в трубе).
	public CompoundTag writeCustomData(HolderLookup.Provider registries) {
		return new CompoundTag();
	}

	public void appendDroppedStacks(List<ItemStack> droppedStacks) {}

	/**
	 * Return true if something was done.
	 */
	public boolean customUse(PipeBlockEntity pipe, Player player, InteractionHand hand, @Nullable Direction hitDirection) {
		return false;
	}

	// Хук выгрузки узла из мира (убирает тикающие обработчики и т.п.).
	public void onUnload() {}
}
