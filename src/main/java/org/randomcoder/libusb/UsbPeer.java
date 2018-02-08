package org.randomcoder.libusb;

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public interface UsbPeer extends Library {

	int libusb_init(PointerByReference contextRef);

	void libusb_exit(Pointer context);

	void libusb_set_debug(Pointer context, int level);

	long libusb_get_device_list(Pointer context, PointerByReference listRef);

	void libusb_free_device_list(Pointer list, int unrefDevices);

	int libusb_get_device_descriptor(Pointer device, UsbDeviceDescriptor descRef);

	int libusb_open(Pointer device, PointerByReference deviceHandleRef);

	void libusb_close(Pointer deviceHandle);

	int libusb_claim_interface(Pointer deviceHandle, int interfaceNumber);

	int libusb_release_interface(Pointer deviceHandle, int interfaceNumber);

	int libusb_bulk_transfer(
			Pointer deviceHandle,
			byte endpoint,
			Pointer data,
			int length,
			IntByReference transferred,
			int timeout);

}