package org.randomcoder.midi.mac.corefoundation;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

public class DirectCoreFoundationPeer implements CoreFoundationPeer {

  static {
    Native.register(CoreFoundationServiceFactory.LIBRARY_NAME);
  }

  @Override
  public native CFStringRef CFStringCreateWithCharacters(Pointer alloc,
      char[] chars, int numChars);

  @Override public native void CFStringGetCharacters(CFStringRef theString,
      CFRange.ByValue range, Pointer buffer);

  @Override
  public native Pointer CFStringGetCharactersPtr(CFStringRef theString);

  @Override public native int CFStringGetLength(CFStringRef theString);

  @Override public native Pointer CFRetain(Pointer cf);

  @Override public native void CFRelease(Pointer cf);

  @Override public native int CFGetRetainCount(Pointer cf);

  @Override public native CFRange.ByValue __CFRangeMake(long loc, long len);

  @Override public native void CFRunLoopRun();

  @Override
  public native int CFRunLoopRunInMode(CFStringRef mode, double seconds,
      boolean returnAfterSourceHandled);

  @Override public native Pointer CFRunLoopGetCurrent();

  @Override public native void CFRunLoopStop(Pointer rl);

  @Override
  public native Pointer CFRunLoopSourceCreate(Pointer alloc, int order,
      CFRunLoopSourceContext context);

  @Override public native void CFRunLoopAddSource(Pointer rl, Pointer source,
      CFStringRef mode);

  @Override public native void CFRunLoopRemoveSource(Pointer rl, Pointer source,
      CFStringRef mode);

  @Override public native void CFRunLoopSourceSignal(Pointer source);

}
