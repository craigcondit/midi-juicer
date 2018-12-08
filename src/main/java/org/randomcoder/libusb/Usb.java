package org.randomcoder.libusb;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import java.util.Objects;

public class Usb {

  private static Usb INSTANCE = new Usb();

  public static Usb getInstance() {
    return INSTANCE;
  }

  static void setInstance(Usb instance) {
    INSTANCE = instance;
  }

  Usb() {

  }

  UsbPeer peer() {
    return UsbServiceFactory.getPeer();
  }

  public Pointer init() throws UsbException {
    PointerByReference contextRef = new PointerByReference();
    int result = peer().libusb_init(contextRef);
    if (result != 0) {
      throw UsbException.fromError(result);
    }
    return contextRef.getValue();
  }

  public void destroy(Pointer context) {
    peer().libusb_exit(context);
  }

  public void setLogLevel(Pointer context, UsbLogLevel level) {
    Objects.requireNonNull(level);
    peer().libusb_set_debug(context, level.ordinal());
  }

  public UsbDeviceDescriptors getDeviceDescriptors(Pointer context)
      throws UsbException {
    UsbPeer peer = peer();

    int result;

    PointerByReference listRef = new PointerByReference();
    long count = peer.libusb_get_device_list(context, listRef);
    if (count < 0L) {
      throw UsbException.fromError((int) count);
    }
    Pointer list = listRef.getValue();

    boolean cleanup = true;
    try {
      Pointer[] devices = list.getPointerArray(0L, (int) count);
      UsbDeviceDescriptor[] dds = new UsbDeviceDescriptor[(int) count];
      for (int i = 0; i < devices.length; i++) {
        UsbDeviceDescriptor dd = new UsbDeviceDescriptor();
        result = peer.libusb_get_device_descriptor(devices[i], dd);
        if (result < 0) {
          throw UsbException.fromError(result);
        }
        dd.handle = devices[i];
        dds[i] = dd;
      }
      cleanup = false;
      return new UsbDeviceDescriptors(list, dds);
    } finally {
      if (cleanup) {
        peer.libusb_free_device_list(list, 1);
      }
    }
  }

  public void destroyDeviceDescriptors(UsbDeviceDescriptors descriptors) {
    peer().libusb_free_device_list(descriptors.handle, 1);
  }

  public Pointer openDevice(UsbDeviceDescriptor descriptor)
      throws UsbException {
    PointerByReference deviceHandleRef = new PointerByReference();
    int result = peer().libusb_open(descriptor.handle, deviceHandleRef);
    if (result != 0) {
      throw UsbException.fromError(result);
    }
    return deviceHandleRef.getValue();
  }

  public void closeDevice(Pointer handle) {
    peer().libusb_close(handle);
  }

  public void claimInterface(Pointer handle, int interfaceNumber)
      throws UsbException {
    int result = peer().libusb_claim_interface(handle, interfaceNumber);
    if (result != 0) {
      throw UsbException.fromError(result);
    }
  }

  public void releaseInterface(Pointer handle, int interfaceNumber)
      throws UsbException {
    int result = peer().libusb_release_interface(handle, interfaceNumber);
    if (result != 0) {
      throw UsbException.fromError(result);
    }
  }

  public void bulkTransfer(Pointer deviceHandle, byte endpoint, Pointer buffer,
      int length, IntByReference transferred, int timeout) throws UsbException {

    int result = peer()
        .libusb_bulk_transfer(deviceHandle, endpoint, buffer, length,
            transferred, timeout);

    if (result != 0) {
      throw UsbException.fromError(result);
    }
  }

}
