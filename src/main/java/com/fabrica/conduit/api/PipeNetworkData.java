package com.fabrica.conduit.api;

/**
 * Extra per-network data, serialized with the network. Must be immutable.
 */
public interface PipeNetworkData {
	PipeNetworkData clone();
}
