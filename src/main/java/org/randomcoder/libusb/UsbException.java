package org.randomcoder.libusb;

import java.util.Optional;

public class UsbException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final int errorCode;

	public UsbException(int errorCode) {
		super();
		this.errorCode = errorCode;
	}

	public UsbException(String message, int errorCode, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	public UsbException(String message, int errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	public UsbException(int errorCode, Throwable cause) {
		super(cause);
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public static UsbException fromError(int errorCode) {
		return Optional.of(UsbError.byErrorCode(errorCode))
				.filter(e -> e != UsbError.OTHER)
				.map(e -> new UsbException(e.description(), errorCode))
				.orElseGet(() -> new UsbException("Unknown error", errorCode));
	}
}
