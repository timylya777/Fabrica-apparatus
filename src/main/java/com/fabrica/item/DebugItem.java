package com.fabrica.item;

import com.fabrica.api.energy.EnergyContainer;
import com.fabrica.api.energy.EnergyConsumer;
import com.fabrica.block.machine.EnergyMachineBlockEntity;
import com.fabrica.block.machine.fuel.AbstractFuelGeneratorBlockEntity;
import com.fabrica.cable.CableBlockEntity;
import com.fabrica.cable.CableNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DebugItem extends Item {

    public DebugItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!level.isClientSide()) {
            BlockPos pos = context.getClickedPos();
            BlockState state = level.getBlockState(pos);

            StringBuilder sb = new StringBuilder();
            sb.append(state.getBlock().getName().getString())
                    .append(" @ ").append(pos.getX()).append(" ").append(pos.getY()).append(" ").append(pos.getZ());

            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CableBlockEntity cable) {
                CableNetwork network = cable.getNetwork();
                sb.append(" | NET: ").append(network.getBuffer().getEnergy())
                        .append("/").append(network.getBuffer().getCapacity())
                        .append(" EU, cables=").append(network.getMemberCount())
                        .append(", share=").append(network.getShare());
            }
            if (be instanceof EnergyMachineBlockEntity machine) {
                EnergyContainer container = machine.getEnergyContainer();
                sb.append(" | ENERGY: ").append(container.getEnergy())
                        .append("/").append(container.getCapacity())
                        .append(" EU, tier=").append(container.getTier().name());
            }
            if (be instanceof EnergyConsumer consumer) {
                sb.append(", demand=").append(consumer.getEnergyDemand());
            }
            if (be instanceof AbstractFuelGeneratorBlockEntity generator) {
                sb.append(" | BURN: ").append(generator.isBurning())
                        .append(", burnTime=").append(generator.getBurnTime())
                        .append("/").append(generator.getTotalBurnTime());
            }

            Player player = context.getPlayer();
            if (player != null) {
                player.sendSystemMessage(Component.literal("[Fabrica] " + sb));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
