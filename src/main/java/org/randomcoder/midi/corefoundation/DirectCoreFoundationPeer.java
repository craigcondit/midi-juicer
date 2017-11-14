package org.randomcoder.midi.corefoundation;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

public class DirectCoreFoundationPeer implements CoreFoundationPeer {

	static {
		Native.register(CoreFoundationServiceFactory.LIBRARY_NAME);
	}

	public native CFStringRef CFStringCreateWithCharacters(Pointer alloc, char[] chars, int numChars);

	public native void CFStringGetCharacters(CFStringRef theString, CFRange.ByValue range, Pointer buffer);

	public native Pointer CFStringGetCharactersPtr(CFStringRef theString);

	public native int CFStringGetLength(CFStringRef theString);

	public native Pointer CFRetain(Pointer cf);

	public native void CFRelease(Pointer cf);

	public native int CFGetRetainCount(Pointer cf);

	public native CFRange.ByValue __CFRangeMake(long loc, long len);

	public native void CFRunLoopRun();

	public native Pointer CFRunLoopGetCurrent();

	public native void CFRunLoopStop(Pointer rl);
}
