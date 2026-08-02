package com.fabrica.conduit.api;

// Дополнительные данные конкретной сети, сохраняются вместе с сетью в NBT;
// обязаны быть неизменяемыми (immutable).
/**
 * Extra per-network data, serialized with the network. Must be immutable.
 */
public interface PipeNetworkData {
	PipeNetworkData clone();
}
