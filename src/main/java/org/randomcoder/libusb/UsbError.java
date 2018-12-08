package org.randomcoder.libusb;

import java.util.Arrays;

public enum UsbError {

  SUCCESS(0, "Success"), IO(-1, "I/O error"), INVALID_PARAM(-2,
      "Invalid parameter"), ERROR_ACCESS(-3,
      "Insufficient permissions"), NO_DEVICE(-4, "No such device"), NOT_FOUND(
      -5, "Entity not found"), BUSY(-6, "Resource busy"), TIMEOUT(-7,
      "Operation timed out"), OVERFLOW(-8, "Overflow"), PIPE(-9,
      "Pipe error"), INTERRUPTED(-10, "System call interrupted"), NO_MEM(-11,
      "Insufficient memory"), NOT_SUPPORTED(-12,
      "Operation not supported or unimplemented on this platform"), OTHER(-99,
      "Other error");

  private final int errorCode;
  private final String description;

  UsbError(final int errorCode, String description) {
    this.errorCode = errorCode;
    this.description = description;
  }

  public int errorCode() {
    return errorCode;
  }

  public String description() {
    return description;
  }

  public static UsbError byErrorCode(final int intValue) {
    return Arrays.stream(UsbError.values()).filter(e -> e.errorCode == intValue)
        .findFirst().orElse(OTHER);
  }

}
