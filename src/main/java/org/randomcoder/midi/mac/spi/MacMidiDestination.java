package org.randomcoder.midi.mac.spi;

public class MacMidiDestination extends AbstractMacMidiDevice {

	public MacMidiDestination(MacMidiDeviceInfo info) {
		super(info);
	}

	@Override
	public int getMaxReceivers() {
		return -1;
	}

	@Override
	public int getMaxTransmitters() {
		return 0;
	}

}
