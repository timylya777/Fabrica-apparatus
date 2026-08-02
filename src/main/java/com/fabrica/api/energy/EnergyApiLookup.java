package com.fabrica.api.energy;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Lookup-и энергии мода на основе Fabric BlockApiLookup.
 * Три вида API: PRODUCER (генераторы), CONSUMER (потребители),
 * CONTAINER (универсальные ёмкости энергии). Позволяет кабелям и машинам
 * находить энергетические компоненты соседних блоков.
 */
public final class EnergyApiLookup {

    /** Идентификаторы API в реестре Fabric. */
    public static final Identifier PRODUCER_ID = Identifier.fromNamespaceAndPath("fabrica_apparatus", "energy_producer");
    public static final Identifier CONSUMER_ID = Identifier.fromNamespaceAndPath("fabrica_apparatus", "energy_consumer");
    public static final Identifier CONTAINER_ID = Identifier.fromNamespaceAndPath("fabrica_apparatus", "energy_container");

    /** Lookup производителей энергии (у генераторов). */
    public static final BlockApiLookup<EnergyProducer, @Nullable Direction> PRODUCER =
        BlockApiLookup.get(PRODUCER_ID, EnergyProducer.class, Direction.class);

    /** Lookup потребителей энергии (у электропечи). */
    public static final BlockApiLookup<EnergyConsumer, @Nullable Direction> CONSUMER =
        BlockApiLookup.get(CONSUMER_ID, EnergyConsumer.class, Direction.class);

    /** Lookup универсальных контейнеров энергии (ёмкость, вставка/извлечение). */
    public static final BlockApiLookup<EnergyContainer, @Nullable Direction> CONTAINER =
        BlockApiLookup.get(CONTAINER_ID, EnergyContainer.class, Direction.class);

    private EnergyApiLookup() {
    }
}
