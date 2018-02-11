package org.randomcoder.midi.samples;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

import org.randomcoder.midi.mac.MacMidi;

public class MidiPipe {

	public static void main(String[] args) throws Exception {
		if (MacMidi.isAvailable()) {
			MacMidi.setDebug(true);
			MacMidi.init();

			MacMidi.addSetupChangedListener(() -> {
				System.out.println("setup changed");
			});
		}

		System.out.println("MIDI initialized");

		try {
			MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
			for (MidiDevice.Info info : infos) {
				System.out.printf("%s: vendor=%s, description=%s%n",
						info.getName(), info.getVendor(), info.getDescription());
			}

			Optional<MidiDevice> input = getInput(infos);
			Optional<MidiDevice> output = getOutput(infos);

			if (input.isPresent() && output.isPresent()) {
				try (MidiDevice inDev = input.get(); Transmitter transmitter = inDev.getTransmitter()) {
					System.out.printf("Opened transmitter for device: %s%n", inDev);
					try (MidiDevice outDev = output.get(); Receiver receiver = outDev.getReceiver()) {
						System.out.printf("Opened receiver for device: %s%n", outDev);

						transmitter.setReceiver(output.get().getReceiver());
						System.out.println("Connected input -> output");
					}

					while (true) {
						Thread.sleep(1000L);
					}
				}
			} else {
				System.out.println("WARNING: Did not find all devices");
			}

		} finally {
			MacMidi.shutdown();
		}
	}

	private static Optional<MidiDevice> getInput(MidiDevice.Info[] infos) {
		List<MidiDevice.Info> deviceInfos = Arrays.stream(MidiSystem.getMidiDeviceInfo())
				.filter(MacMidi::isMacMidiDevice)
				.filter(di -> "MIDI1".equals(di.getName()))
				.filter(di -> "Nektar".equals(di.getVendor()))
				.filter(di -> "Impact GX49 MIDI1".equals(di.getDescription()))
				.collect(Collectors.toList());

		return deviceInfos.stream()
				.map(di -> {
					try {
						return MidiSystem.getMidiDevice(di);
					} catch (Exception e) {
						return null;
					}
				})
				.filter(Objects::nonNull)
				.filter(d -> d.getMaxTransmitters() != 0)
				.findFirst();
	}

	private static Optional<MidiDevice> getOutput(MidiDevice.Info[] infos) {
		List<MidiDevice.Info> deviceInfos = Arrays.stream(MidiSystem.getMidiDeviceInfo())
				.filter(MacMidi::isMacMidiDevice)
				.filter(di -> "Out".equals(di.getName()))
				.filter(di -> "E-MU Systems,Inc.".equals(di.getVendor()))
				.filter(di -> "E-MU XMidi1X1 Tab Out".equals(di.getDescription()))
				.collect(Collectors.toList());

		return deviceInfos.stream()
				.map(di -> {
					try {
						return MidiSystem.getMidiDevice(di);
					} catch (Exception e) {
						return null;
					}
				})
				.filter(Objects::nonNull)
				.filter(d -> d.getMaxReceivers() != 0)
				.findFirst();
	}
}
