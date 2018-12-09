package org.randomcoder.libusb;

public enum UsbLogLevel {
  /* no messages printed */
  NONE, /* errors to stderr */
  ERROR, /* warnings and errors to stderr */
  WARNING, /* info to stdout, warning/error to stderr */
  INFO, /* debug,info to stdout, warning/error to stderr */
  DEBUG;
}
