package org.randomcoder.midi.mac.coremidi;

import java.util.Optional;

public class CoreMidiException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final int errorCode;

	public CoreMidiException(int errorCode) {
		super();
		this.errorCode = errorCode;
	}

	public CoreMidiException(String message, int errorCode, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	public CoreMidiException(String message, int errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	public CoreMidiException(int errorCode, Throwable cause) {
		super(cause);
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public static CoreMidiException fromError(int errorCode) {
		return Optional.of(CoreMidiError.byErrorCode(errorCode))
				.filter(e -> e != CoreMidiError.kUnknownError)
				.map(e -> new CoreMidiException(e.description(), errorCode))
				.orElseGet(() -> new CoreMidiException("Unknown error", errorCode));
	}
}
