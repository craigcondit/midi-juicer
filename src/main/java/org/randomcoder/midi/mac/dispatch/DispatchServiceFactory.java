package org.randomcoder.midi.mac.dispatch;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

public class DispatchServiceFactory {

	private static final AtomicReference<DispatchPeer> PEER = new AtomicReference<>();
	private static final AtomicReference<NativeLibrary> NATIVE_LIBRARY = new AtomicReference<>();

	public static final String LIBRARY_NAME = "/usr/lib/system/libdispatch.dylib";

	public static DispatchPeer getPeer() {
		return getOrCreate(PEER, DispatchServiceFactory::createNativePeer);
	}

	public static NativeLibrary getNativeLibrary() {
		return getOrCreate(NATIVE_LIBRARY, DispatchServiceFactory::createNativeLibrary);
	}

	static DispatchPeer createNativePeer() {
		return Native.loadLibrary(LIBRARY_NAME, DispatchPeer.class);
	}

	static NativeLibrary createNativeLibrary() {
		return NativeLibrary.getInstance(LIBRARY_NAME);
	}

	static void setPeer(DispatchPeer peer) {
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
