package org.randomcoder.midi.mac;

import javax.sound.midi.MidiDevice;

import org.randomcoder.midi.mac.coremidi.CoreMidi;
import org.randomcoder.midi.mac.spi.AbstractMacMidiDevice;
import org.randomcoder.midi.mac.spi.MacMidiDeviceInfo;

public class MacMidi {

	public static boolean isAvailable() {
		try {
			CoreMidi.getInstance().getNumberOfDevices();
		} catch (Throwable t) {
			return false;
		}

		return true;
	}

	public static boolean isMacMidiDevice(MidiDevice.Info info) {
		return info instanceof MacMidiDeviceInfo;
	}

	public static boolean isMacMidiDevice(MidiDevice device) {
		return device instanceof AbstractMacMidiDevice;
	}

}
