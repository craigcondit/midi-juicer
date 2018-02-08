package org.randomcoder.libusb;

public class BcdUtils {
	public static String decodeBcdUsb(short bcd) {
		int major = (bcd & 0xff00) >> 8;
		int minor = (bcd & 0x00f0) >> 4;
		int revision = (bcd & 0x000f);
		return String.format("%d.%d%d", major, minor, revision);
	}
}
