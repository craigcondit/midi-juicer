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
import org.randomcoder.midi.mac.RunLoop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MidiPipe {

	private static final Logger LOG = LoggerFactory.getLogger(MidiPipe.class);

	public static void main(String[] args) throws Exception {

		try (RunLoop rl = RunLoop.spawn(true)) {
			if (MacMidi.available()) {
				MacMidi.init();

				MacMidi.addSetupChangedListener(e -> {
					LOG.info("MIDI setup changed: {}", e);
				});
			}

			LOG.info("Ready");

			MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
			for (MidiDevice.Info info : infos) {
				LOG.info("Found device: {}, vendor={}, description={}",
						info.getName(), info.getVendor(), info.getDescription());
			}

			Optional<MidiDevice> input = getInput(infos);
			Optional<MidiDevice> output = getOutput(infos);

			if (input.isPresent() && output.isPresent()) {
				try (MidiDevice inDev = input.get(); Transmitter transmitter = inDev.getTransmitter()) {

					LOG.debug("Opened transmitter for device: {}", inDev);
					try (MidiDevice outDev = output.get(); Receiver receiver = outDev.getReceiver()) {
						LOG.debug("Opened receiver for device: {}", outDev);

						transmitter.setReceiver(output.get().getReceiver());
						LOG.debug("Connected input -> output");
					}

					while (true) {
						Thread.sleep(1000L);
					}
				}
			} else {
				LOG.warn("Did not find all devices");
			}
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
