package org.randomcoder.midi.mac.spi;

public class MacMidiSource extends AbstractMacMidiDevice {

	public MacMidiSource(MacMidiDeviceInfo info) {
		super(info);
	}

	@Override
	public int getMaxReceivers() {
		return 0;
	}

	@Override
	public int getMaxTransmitters() {
		return -1;
	}

}
