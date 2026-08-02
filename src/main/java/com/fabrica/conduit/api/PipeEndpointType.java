package com.fabrica.conduit.api;

/**
 * Отвечает за типы коннекторов на стороне трубы: PIPE — соединение с другой
 * трубой (линк сети), BLOCK — подключение к блоку без направления
 * (электричество), BLOCK_IN/BLOCK_OUT/BLOCK_IN_OUT — подключение к машине
 * с режимом ввода/вывода (предметы, жидкости). Значения кодируются в байты
 * при синхронизации с клиентом.
 */
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
