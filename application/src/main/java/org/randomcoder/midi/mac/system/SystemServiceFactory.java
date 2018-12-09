package org.randomcoder.midi.mac.system;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class SystemServiceFactory {
  private static final AtomicReference<SystemPeer> PEER =
      new AtomicReference<>();
  private static final AtomicReference<NativeLibrary> NATIVE_LIBRARY =
      new AtomicReference<>();

  public static final String LIBRARY_NAME = "System";

  public static SystemPeer getPeer() {
    return getOrCreate(PEER, SystemServiceFactory::createDirectPeer);
  }

  public static NativeLibrary getNativeLibrary() {
    return getOrCreate(NATIVE_LIBRARY,
        SystemServiceFactory::createNativeLibrary);
  }

  static SystemPeer createDirectPeer() {
    return new DirectSystemPeer();
  }

  static SystemPeer createNativePeer() {
    return Native.load(LIBRARY_NAME, SystemPeer.class);
  }

  static NativeLibrary createNativeLibrary() {
    return NativeLibrary.getInstance(LIBRARY_NAME);
  }

  static void setPeer(SystemPeer peer) {
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
