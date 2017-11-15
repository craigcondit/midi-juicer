package org.randomcoder.midi.corefoundation;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

public class DirectCoreFoundationPeer implements CoreFoundationPeer {

    static {
	Native.register(CoreFoundationServiceFactory.LIBRARY_NAME);
    }

    @Override
    public native CFStringRef CFStringCreateWithCharacters(Pointer alloc, char[] chars, int numChars);

    @Override
    public native void CFStringGetCharacters(CFStringRef theString, CFRange.ByValue range, Pointer buffer);

    @Override
    public native Pointer CFStringGetCharactersPtr(CFStringRef theString);

    @Override
    public native int CFStringGetLength(CFStringRef theString);

    @Override
    public native Pointer CFRetain(Pointer cf);

    @Override
    public native void CFRelease(Pointer cf);

    @Override
    public native int CFGetRetainCount(Pointer cf);

    @Override
    public native CFRange.ByValue __CFRangeMake(long loc, long len);

    @Override
    public native void CFRunLoopRun();

    @Override
    public native Pointer CFRunLoopGetCurrent();

    @Override
    public native void CFRunLoopStop(Pointer rl);
}
