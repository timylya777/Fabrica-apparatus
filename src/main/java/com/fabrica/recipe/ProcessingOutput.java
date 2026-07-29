
package com.fabrica.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record ProcessingOutput(ItemStack stack, float chance) {
    public static final Codec<ProcessingOutput> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.fieldOf("stack").forGetter(ProcessingOutput::stack),
            Codec.FLOAT.fieldOf("chance").forGetter(ProcessingOutput::chance)
    ).apply(instance, ProcessingOutput::new));

    public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, ProcessingOutput> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, ProcessingOutput::stack,
            ByteBufCodecs.FLOAT, ProcessingOutput::chance,
            ProcessingOutput::new
    );
}