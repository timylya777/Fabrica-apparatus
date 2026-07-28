package com.fabrica.energy;

public interface APNode {
    APStorage getStorage();
}
// Маркерные интерфейсы для удобства
public interface APProvider extends APNode {} // Генераторы
public interface APConsumer extends APNode {} // Машины