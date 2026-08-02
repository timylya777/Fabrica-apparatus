package com.fabrica.me;

/**
 * Маркер-интерфейс узла ME-сети: блок, реализующий его (например, ME-сетка),
 * участвует в обходе сети MeNetwork и предоставляет своё хранилище.
 */
public interface MeNetworkNode {
    /** Хранилище предметов, которое узел подключает к сети. */
    MeStorage getMeStorage();
}
