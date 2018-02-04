package org.randomcoder.midi;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
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

		for (MidiDevice device : devices) {
			System.out.printf("Name: '%s'%n", device.getDeviceInfo().getName());
			System.out.printf("  vendor: '%s'%n", device.getDeviceInfo().getVendor());
			System.out.printf("  description: '%s'%n", device.getDeviceInfo().getDescription());
			System.out.printf("  version: '%s'%n", device.getDeviceInfo().getVersion());
			System.out.printf("  MacMidi device: %s%n", MacMidi.isMacMidiDevice(device));
			System.out.printf("  class: %s%n", device.getClass());
			System.out.printf("  maxReceivers: %d%n", device.getMaxReceivers());
			System.out.printf("  maxTransmitters: %d%n", device.getMaxTransmitters());
			System.out.printf("  isOpen: %s%n", device.isOpen());
			System.out.printf("  microSecondPosition: %d%n", device.getMicrosecondPosition());
		}

		if (devices.isEmpty()) {
			System.out.println("No matching devices found");
			return;
		}

		MidiDevice device = devices.get(0);
		Receiver receiver = device.getReceiver();

		int minNote = 60;
		int maxNote = 72;
		int delayMs = 250;
		
		for (int i = minNote; i <= maxNote; i++) {
			// send note off for previous note / note on for current one
			MidiMessage noteOff = new ShortMessage(ShortMessage.NOTE_OFF, 0, Math.max(0, i - 1), 0);
			receiver.send(noteOff, device.getMicrosecondPosition());

			MidiMessage noteOn = new ShortMessage(ShortMessage.NOTE_ON, 0, i, 127);
			receiver.send(noteOn, device.getMicrosecondPosition());

			try {
				Thread.sleep(delayMs);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (i == maxNote) {
				MidiMessage noteOff2 = new ShortMessage(ShortMessage.NOTE_OFF, 0, i, 0);
				receiver.send(noteOff2, device.getMicrosecondPosition());
			}
		}
		
		receiver.close();
		device.close();
	}

}
