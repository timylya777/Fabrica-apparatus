package com.fabrica.me;

/**
 * Тиры ME-дисков: определяют ёмкость диска в единицах хранения.
 * Ёмкость = unit * 1024 штук предметов (BASIC 64K, ADVANCED 256K, ELITE 1024K).
 */
public enum MeStorageDiskTier {
    BASIC(64),
    ADVANCED(256),
    ELITE(1024);

    /** Базовая единица объёма в "килоштуках". */
    private final long unit;

    MeStorageDiskTier(long unit) {
        this.unit = unit;
    }

    /** Вернуть базовую единицу тира. */
    public long getUnit() {
        return unit;
    }

    /** Итоговая ёмкость диска этого тира в штуках предметов. */
    public long getCapacity() {
        return unit * 1024L;
    }
}
