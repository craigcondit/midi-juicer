package org.randomcoder.midi.mac.corefoundation;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class CoreFoundationServiceFactory {
  private static final AtomicReference<CoreFoundationPeer> PEER =
      new AtomicReference<>();
  private static final AtomicReference<NativeLibrary> NATIVE_LIBRARY =
      new AtomicReference<>();

  public static final String LIBRARY_NAME = "CoreFoundation";

  public static CoreFoundationPeer getPeer() {
    return getOrCreate(PEER, CoreFoundationServiceFactory::createDirectPeer);
  }

  public static NativeLibrary getNativeLibrary() {
    return getOrCreate(NATIVE_LIBRARY,
        CoreFoundationServiceFactory::createNativeLibrary);
  }

  static CoreFoundationPeer createDirectPeer() {
    return new DirectCoreFoundationPeer();
  }

  static CoreFoundationPeer createNativePeer() {
    return Native.load(LIBRARY_NAME, CoreFoundationPeer.class);
  }

  static NativeLibrary createNativeLibrary() {
    return NativeLibrary.getInstance(LIBRARY_NAME);
  }

  static void setPeer(CoreFoundationPeer peer) {
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

  private static <T> T getOrCreate(AtomicReference<T> ref,
      Supplier<T> supplier) {
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
