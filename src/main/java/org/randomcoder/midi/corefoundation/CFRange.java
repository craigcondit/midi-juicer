package org.randomcoder.midi.corefoundation;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class CFRange extends Structure {

	public long loc;
	public long len;

	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("loc", "len");
	}

	public CFRange() {
	}

	public CFRange(Pointer pointer, int offset) {
		super();
		useMemory(pointer, offset);
		read();
	}

	public CFRange(long loc, long len) {
		super();
		this.loc = loc;
		this.len = len;
		write();
	}

	public static class ByReference extends CFRange implements Structure.ByReference {
		public ByReference() {
		}

		public ByReference(CFRange struct) {
			super(struct.getPointer(), 0);
		}
	}

	public static class ByValue extends CFRange implements Structure.ByValue {
		public ByValue() {
		}

		public ByValue(CFRange struct) {
			super(struct.getPointer(), 0);
		}
	}

}
