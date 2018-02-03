package org.randomcoder.midi;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;

import org.randomcoder.midi.mac.MacMidi;

public class ApiProvider {

	public static void main(String[] args) throws Exception {

		System.out.printf("MacMidi available: %s%n", MacMidi.isAvailable());
		System.out.printf("MacMidi available: %s%n", MacMidi.isAvailable());
		System.out.printf("MacMidi available: %s%n", MacMidi.isAvailable());

		MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();

		for (MidiDevice.Info info : infos) {
			System.out.printf("%s: name=%s, vendor=%s, desc=%s, version=%s%n",
					info,
					info.getName(),
					info.getVendor(),
					info.getDescription(),
					info.getVersion());

			MidiDevice device = null;
			try {
				device = MidiSystem.getMidiDevice(info);
			} catch (Exception e) {
				System.out.printf("  %s%n", e.toString());
				continue;
			}

			System.out.printf("  MacMidi device: %s%n", MacMidi.isMacMidiDevice(device));
			System.out.printf("  class: %s%n", device.getClass());
			System.out.printf("  maxReceivers: %d%n", device.getMaxReceivers());
			System.out.printf("  maxTransmitters: %d%n", device.getMaxTransmitters());
			System.out.printf("  isOpen: %s%n", device.isOpen());
			System.out.printf("  microSecondPosition: %d%n", device.getMicrosecondPosition());
		}

	}

}
