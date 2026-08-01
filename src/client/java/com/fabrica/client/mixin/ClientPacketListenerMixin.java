package com.fabrica.client.mixin;

import com.fabrica.conduit.impl.PipeBlockEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * In 26.2, handleBlockEntityData does not invalidate the render of the section
 * when a block entity update arrives. Pipes rely on those updates to know their
 * connections, so without this the section would keep rendering the stale
 * (empty) connections until some unrelated rebuild happens, making pipes appear
 * transparent for a while. Force a re-render of the section when the updated
 * block entity is a pipe.
 */
@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
	@Inject(method = "handleBlockEntityData(Lnet/minecraft/network/protocol/game/ClientboundBlockEntityDataPacket;)V", at = @At("RETURN"))
	private void fabrica_rerenderPipeSection(ClientboundBlockEntityDataPacket packet, CallbackInfo ci) {
		ClientPacketListener self = (ClientPacketListener) (Object) this;
		ClientLevel level = self.getLevel();
		if (level == null) return;
		BlockPos pos = packet.getPos();
		if (level.getBlockEntity(pos) instanceof PipeBlockEntity) {
			level.setSectionDirtyWithNeighbors(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4);
		}
	}
}
