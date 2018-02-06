package org.randomcoder.midi.mac.system;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class MachTimebaseInfo extends Structure {
	public int numer;
	public int denom;

	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("numer", "denom");
	}

	public MachTimebaseInfo() {
	}

	public MachTimebaseInfo(Pointer pointer, int offset) {
		super();
		useMemory(pointer, offset);
		read();
	}

	public MachTimebaseInfo(int numer, int denom) {
		super();
		this.numer = numer;
		this.denom = denom;
		write();
	}

	public static class ByReference extends MachTimebaseInfo implements Structure.ByReference {
		public ByReference() {
		}

		public ByReference(MachTimebaseInfo struct) {
			super(struct.getPointer(), 0);
		}
	}

	public static class ByValue extends MachTimebaseInfo implements Structure.ByValue {
		public ByValue() {
		}

		public ByValue(MachTimebaseInfo struct) {
			super(struct.getPointer(), 0);
		}
	}

}
