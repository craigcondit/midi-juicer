package org.randomcoder.midi.mac.spi;

import javax.sound.midi.MidiDevice;

public class MacMidiDeviceInfo extends MidiDevice.Info {

	protected MacMidiDeviceInfo(String name, String vendor, String description, String version) {
		super(name, vendor, description, version);
	}

}
