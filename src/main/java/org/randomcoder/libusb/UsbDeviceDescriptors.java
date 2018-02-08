package org.randomcoder.libusb;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.sun.jna.Pointer;

public class UsbDeviceDescriptors {

	final Pointer handle;

	public final UsbDeviceDescriptor[] descriptors;

	UsbDeviceDescriptors(Pointer handle, UsbDeviceDescriptor[] descriptors) {
		this.handle = handle;
		this.descriptors = descriptors;
	}

	@Override
	public String toString() {
		return Arrays.stream(descriptors)
				.map(UsbDeviceDescriptor::toString)
				.collect(Collectors.joining(String.format("%n")));
	}
}
