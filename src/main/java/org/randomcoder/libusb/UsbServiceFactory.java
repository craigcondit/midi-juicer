package org.randomcoder.libusb;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

public class UsbServiceFactory {

	private static final AtomicReference<UsbPeer> PEER = new AtomicReference<>();
	private static final AtomicReference<NativeLibrary> NATIVE_LIBRARY = new AtomicReference<>();

	public static final String LIBRARY_NAME = "usb-1.0.0";

	public static UsbPeer getPeer() {
		return getOrCreate(PEER, UsbServiceFactory::createNativePeer);
	}

	public static NativeLibrary getNativeLibrary() {
		return getOrCreate(NATIVE_LIBRARY, UsbServiceFactory::createNativeLibrary);
	}

	static UsbPeer createNativePeer() {
		return Native.loadLibrary(LIBRARY_NAME, UsbPeer.class);
	}

	static NativeLibrary createNativeLibrary() {
		NativeLibrary.addSearchPath(LIBRARY_NAME, "/opt/local/lib");
		NativeLibrary.addSearchPath(LIBRARY_NAME, "/usr/local/lib");
		return NativeLibrary.getInstance(LIBRARY_NAME);
	}

	static void setPeer(UsbPeer peer) {
		forceSet(PEER, peer);
	}

	static void setNativeLibrary(NativeLibrary lib) {
		forceSet(NATIVE_LIBRARY, lib);
	}

	private static <T> void forceSet(AtomicReference<T> ref, T obj) {
		synchronized (ref) {
			ref.set(obj);
		}
	}

	private static <T> T getOrCreate(AtomicReference<T> ref, Supplier<T> supplier) {
		T obj;
		while ((obj = ref.get()) == null) {
			// if object is null, we synchronize, and test again
			synchronized (ref) {
				obj = ref.get();
				if (obj != null) {
					// some other thread set it already
					break;
				}
				// create and update the reference
				ref.set(supplier.get());
				obj = ref.get();
			}
		}
		return obj;
	}

}
