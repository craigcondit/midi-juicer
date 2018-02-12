package org.randomcoder.midi.samples;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

import org.randomcoder.midi.MidiFilter;
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

						Receiver dest = output.get().getReceiver();
						MidiFilter filter = (m, t) -> {
							if (!(m instanceof ShortMessage)) {
								return new MidiMessage[] { m };
							}
							ShortMessage sm = (ShortMessage) m;
							if (sm.getStatus() == ShortMessage.NOTE_ON || sm.getStatus() == ShortMessage.NOTE_OFF) {
								// note, let's make it a chord
								int note1 = sm.getData1();
								int note2 = note1 + 3;
								int note3 = note1 + 7;
								try {
									return new MidiMessage[] {
											sm,
											new ShortMessage(sm.getStatus(), note2, sm.getData2()),
											new ShortMessage(sm.getStatus(), note3, sm.getData2()) };
								} catch (InvalidMidiDataException e) {
									return null;
								}
							} else {
								return new MidiMessage[] { sm };
							}
						};

						transmitter.setReceiver(MidiFilter.toReceiver(dest, filter));

						LOG.debug("Connected input -> filter -> output");
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
