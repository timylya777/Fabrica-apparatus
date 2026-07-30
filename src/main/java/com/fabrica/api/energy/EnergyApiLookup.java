package com.fabrica.api.energy;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public final class EnergyApiLookup {

    public static final Identifier PRODUCER_ID = Identifier.fromNamespaceAndPath("fabrica_apparatus", "energy_producer");
    public static final Identifier CONSUMER_ID = Identifier.fromNamespaceAndPath("fabrica_apparatus", "energy_consumer");
    public static final Identifier CONTAINER_ID = Identifier.fromNamespaceAndPath("fabrica_apparatus", "energy_container");

    public static final BlockApiLookup<EnergyProducer, @Nullable Direction> PRODUCER =
        BlockApiLookup.get(PRODUCER_ID, EnergyProducer.class, Direction.class);

    public static final BlockApiLookup<EnergyConsumer, @Nullable Direction> CONSUMER =
        BlockApiLookup.get(CONSUMER_ID, EnergyConsumer.class, Direction.class);

    public static final BlockApiLookup<EnergyContainer, @Nullable Direction> CONTAINER =
        BlockApiLookup.get(CONTAINER_ID, EnergyContainer.class, Direction.class);

    private EnergyApiLookup() {
    }
}
