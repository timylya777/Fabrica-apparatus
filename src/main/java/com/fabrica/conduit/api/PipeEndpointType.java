package com.fabrica.conduit.api;

/**
 * The type of an endpoint.
 */
public enum PipeEndpointType {
	PIPE(0),
	BLOCK(1),
	BLOCK_IN(2),
	BLOCK_IN_OUT(3),
	BLOCK_OUT(4);

	private final int id;

	PipeEndpointType(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public static PipeEndpointType byId(int id) {
		return switch (id) {
			case 0 -> PIPE;
			case 1 -> BLOCK;
			case 2 -> BLOCK_IN;
			case 3 -> BLOCK_IN_OUT;
			case 4 -> BLOCK_OUT;
			default -> null;
		};
	}
}
