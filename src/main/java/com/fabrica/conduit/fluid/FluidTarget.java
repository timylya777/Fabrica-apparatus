package com.fabrica.conduit.fluid;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

/**
 * A target to be used during a transfer operation.
 */
class FluidTarget {
	final int priority;
	final Storage<FluidVariant> storage;
	final boolean canExtract;
	final boolean canInsert;

	// A temporary value used to sort fluid targets
	long simulationResult;

	public FluidTarget(int priority, Storage<FluidVariant> storage, boolean canExtract, boolean canInsert) {
		this.priority = priority;
		this.storage = storage;
		this.canExtract = canExtract;
		this.canInsert = canInsert;
	}
}
