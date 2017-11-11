package org.randomcoder.midi.corefoundation;

import java.nio.charset.StandardCharsets;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class CFStringRef extends PointerByReference {

	public CFStringRef() {
	}

	public CFStringRef(Pointer p) {
		setPointer(p);
	}

	public static CFStringRef createNative(String str) {
		final char[] chars = str.toCharArray();
		return CoreFoundationServiceFactory.getPeer().CFStringCreateWithCharacters(null, chars, chars.length);
	}

	@Override
	public String toString() {
		CoreFoundationPeer peer = CoreFoundationServiceFactory.getPeer();

		int len = peer.CFStringGetLength(this);
		if (len == 0) {
			return "";
		}

		long size = ((long) len) * 2L;
		if (size > (long) (Integer.MAX_VALUE)) {
			throw new IllegalArgumentException("Native string too long");
		}

		Memory m = new Memory(size);
		CFRange.ByValue range = new CFRange.ByValue(new CFRange(0, len));
		peer.CFStringGetCharacters(this, range, m);
		return stringFromNativeUtf16(m);
	}

	private static String stringFromNativeUtf16(Memory memory) {
		long size = memory.size();

		// MacOS uses UTF-16 little-endian
		return new String(memory.getByteArray(0L, (int) size), StandardCharsets.UTF_16LE);
	}

}
