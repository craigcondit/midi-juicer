package org.randomcoder.midi.coremidi;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

public class CoreMidiServiceFactory {
	private static final AtomicReference<CoreMidiPeer> PEER = new AtomicReference<>();
	private static final AtomicReference<NativeLibrary> NATIVE_LIBRARY = new AtomicReference<>();
	private static final AtomicReference<CoreMidiPropertyResolver> PROPERTY_RESOLVER = new AtomicReference<>();

	public static final String LIBRARY_NAME = "CoreMIDI";

	public static CoreMidiPeer getPeer() {
		return getOrCreate(PEER, CoreMidiServiceFactory::createDirectPeer);
	}

	public static NativeLibrary getNativeLibrary() {
		return getOrCreate(NATIVE_LIBRARY, CoreMidiServiceFactory::createNativeLibrary);
	}

	public static CoreMidiPropertyResolver getPropertyResolver() {
		return getOrCreate(PROPERTY_RESOLVER, DefaultCoreMidiPropertyResolver::new);
	}

	static CoreMidiPeer createDirectPeer() {
		return new DirectCoreMidiPeer();
	}

	static CoreMidiPeer createNativePeer() {
		return (CoreMidiPeer) Native.loadLibrary(LIBRARY_NAME, CoreMidiPeer.class);
	}

	static NativeLibrary createNativeLibrary() {
		return NativeLibrary.getInstance(LIBRARY_NAME);
	}

	static void setPeer(CoreMidiPeer peer) {
		forceSet(PEER, peer);
	}

	static void setNativeLibrary(NativeLibrary lib) {
		forceSet(NATIVE_LIBRARY, lib);
	}

	static void setPropertyResolver(CoreMidiPropertyResolver resolver) {
		forceSet(PROPERTY_RESOLVER, resolver);
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
