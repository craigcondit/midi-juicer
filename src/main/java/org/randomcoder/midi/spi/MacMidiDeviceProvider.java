package org.randomcoder.midi.spi;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.spi.MidiDeviceProvider;

public class MacMidiDeviceProvider extends MidiDeviceProvider {

	public static final String VENDOR = "";

	@Override
	public MidiDevice getDevice(MidiDevice.Info info) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MidiDevice.Info[] getDeviceInfo() {
		// TODO Auto-generated method stub
		return new MidiDevice.Info[] {
				new MacMidiDeviceInfo("foo", VENDOR, "Test", "1.0")
		};
	}

}
