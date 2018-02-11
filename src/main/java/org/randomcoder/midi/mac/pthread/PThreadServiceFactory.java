package org.randomcoder.midi.mac.pthread;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

public class PThreadServiceFactory {

	private static final AtomicReference<PThreadPeer> PEER = new AtomicReference<>();
	private static final AtomicReference<NativeLibrary> NATIVE_LIBRARY = new AtomicReference<>();

	public static final String LIBRARY_NAME = "/usr/lib/libpthread.dylib";

	public static PThreadPeer getPeer() {
		return getOrCreate(PEER, PThreadServiceFactory::createNativePeer);
	}

	public static NativeLibrary getNativeLibrary() {
		return getOrCreate(NATIVE_LIBRARY, PThreadServiceFactory::createNativeLibrary);
	}

	static PThreadPeer createNativePeer() {
		return Native.loadLibrary(LIBRARY_NAME, PThreadPeer.class);
	}

	static NativeLibrary createNativeLibrary() {
		return NativeLibrary.getInstance(LIBRARY_NAME);
	}

	static void setPeer(PThreadPeer peer) {
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
