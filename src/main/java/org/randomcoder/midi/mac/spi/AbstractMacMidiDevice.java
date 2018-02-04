package org.randomcoder.midi.mac.spi;

import javax.sound.midi.MidiDevice;

import org.randomcoder.midi.mac.coremidi.CoreMidi;

abstract public class AbstractMacMidiDevice implements MidiDevice {
	private final MacMidiDeviceInfo info;
	private final int deviceRef;

	protected AbstractMacMidiDevice(MacMidiDeviceInfo info) {
		this.info = info;
		this.deviceRef = CoreMidi.getInstance().getDeviceRefByUniqueID(info.getUniqueId());
	}

	@Override
	public MacMidiDeviceInfo getDeviceInfo() {
		return info;
	}

	@Override
	public long getMicrosecondPosition() {
		return -1L;
	}

	public int getDeviceRef() {
		return deviceRef;
	}
}
