package org.randomcoder.libusb;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class UsbDeviceDescriptor extends Structure {

	public byte bLength;
	public byte bDescriptorType;
	public short bcdUSB;
	public byte bDeviceClass;
	public byte bDeviceSubClass;
	public byte bDeviceProtocol;
	public byte bMaxPacketSize0;
	public short idVendor;
	public short idProduct;
	public short bcdDevice;
	public byte iManufacturer;
	public byte iProduct;
	public byte iSerial;
	public byte bNumConfigurations;
	Pointer handle;

	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList(
				"bLength", "bDescriptorType", "bcdUSB", "bDeviceClass",
				"bDeviceSubClass", "bDeviceProtocol", "bMaxPacketSize0", "idVendor",
				"idProduct", "bcdDevice", "iManufacturer", "iProduct",
				"iSerial", "bNumConfigurations");
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append(String.format("Device Descriptor:%n"));
		buf.append(String.format("  bLength               %3d%n", bLength));
		buf.append(String.format("  bDescriptorType       %3d%n", bDescriptorType));
		buf.append(String.format("  bcdUSB             %6s%n", BcdUtils.decodeBcdUsb(bcdUSB)));
		buf.append(String.format("  bDeviceClass          %3d%n", bDeviceClass));
		buf.append(String.format("  bDeviceSubClass       %3d%n", bDeviceSubClass));
		buf.append(String.format("  bDeviceProtocol       %3d%n", bDeviceProtocol));
		buf.append(String.format("  bMaxPacketSize0       %3d%n", bMaxPacketSize0));
		buf.append(String.format("  idVendor           0x%04x%n", idVendor));
		buf.append(String.format("  idProduct          0x%04x%n", idProduct));
		buf.append(String.format("  bcdDevice          %6s%n", BcdUtils.decodeBcdUsb(bcdDevice)));
		buf.append(String.format("  iManufacturer         %3d%n", iManufacturer));
		buf.append(String.format("  iProduct              %3d%n", iProduct));
		buf.append(String.format("  iSerial               %3d%n", iSerial));
		buf.append(String.format("  bNumConfigurations    %3d", bNumConfigurations));
		return buf.toString();
	}
}
