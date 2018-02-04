package org.randomcoder.midi;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import org.randomcoder.midi.mac.MacMidi;

public class ApiProvider {

	public static void main(String[] args) throws Exception {

		List<MidiDevice.Info> deviceInfos = Arrays.stream(MidiSystem.getMidiDeviceInfo())
				.filter(MacMidi::isMacMidiDevice)
				.filter(di -> "Out".equals(di.getName()))
				.filter(di -> "E-MU Systems,Inc.".equals(di.getVendor()))
				.filter(di -> "E-MU XMidi1X1 Tab Out".equals(di.getDescription()))
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
				.filter(d -> d.getMaxReceivers() != 0)
				.collect(Collectors.toList());

		if (devices.isEmpty()) {
			System.out.println("No matching devices found");
			return;
		}

		MidiDevice device = devices.get(0);
		Receiver receiver = device.getReceiver();

		int minNote = 60;
		int maxNote = 72;
		int delayMs = 500;

		for (int i = minNote; i <= maxNote; i++) {
			receiver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, i, 127), device.getMicrosecondPosition());
			receiver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, i + 4, 127), device.getMicrosecondPosition());
			receiver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, i + 7, 127), device.getMicrosecondPosition());

			try {
				Thread.sleep(delayMs);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			receiver.send(new ShortMessage(ShortMessage.NOTE_OFF, 0, i, 0), device.getMicrosecondPosition());
			receiver.send(new ShortMessage(ShortMessage.NOTE_OFF, 0, i + 4, 0), device.getMicrosecondPosition());
			receiver.send(new ShortMessage(ShortMessage.NOTE_OFF, 0, i + 7, 0), device.getMicrosecondPosition());
		}

		receiver.close();
		device.close();
	}

}
