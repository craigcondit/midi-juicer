package org.randomcoder.midi.samples;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Transmitter;

import org.randomcoder.midi.mac.MacMidi;

public class MidiInput {

	public static void main(String[] args) throws Exception {

		if (MacMidi.isAvailable()) {
			MacMidi.init();
		}

		try {
			List<MidiDevice.Info> deviceInfos = Arrays.stream(MidiSystem.getMidiDeviceInfo())
					.filter(MacMidi::isMacMidiDevice)
					.filter(di -> "MIDI1".equals(di.getName()))
					.filter(di -> "Nektar".equals(di.getVendor()))
					.filter(di -> "Impact GX49 MIDI1".equals(di.getDescription()))
					.collect(Collectors.toList());

			List<MidiDevice> devices = deviceInfos.stream()
					.map(di -> {
						try {
							return MidiSystem.getMidiDevice(di);
						} catch (Exception e) {
							return null;
						}
					})
					.filter(Objects::nonNull)
					.filter(d -> d.getMaxTransmitters() != 0)
					.collect(Collectors.toList());

			if (devices.isEmpty()) {
				System.out.println("No matching devices found");
				return;
			}

			try (MidiDevice device = devices.get(0); Transmitter transmitter = device.getTransmitter()) {
				System.out.printf("Opened transmitter for device: %s%n", device);
				// TODO do something useful
			}

			Thread.sleep(60_000L);

		} finally {
			MacMidi.shutdown();
		}
	}

}
