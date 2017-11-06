package org.randomcoder.midi.experimental;

import java.util.*;

import javax.sound.midi.*;

import uk.co.xfactorylibrarians.coremidi4j.*;

public class EnumerateMidiDevices {

    public static boolean isCoreMidiLoaded() throws CoreMidiException {
	return CoreMidiDeviceProvider.isLibraryLoaded();
    }

    public static void watchForMidiChanges() throws CoreMidiException {
	CoreMidiDeviceProvider.addNotificationListener(() -> System.out.println("The MIDI environment has changed."));
    }

    public static MidiDevice.Info[] getWorkingDeviceInfo() {
	return CoreMidiDeviceProvider.getMidiDeviceInfo();
    }

    public static void main(String[] args) throws Exception {
	if (isCoreMidiLoaded()) {
	    System.out.println("Loaded coremidi.");
	}
	// watchForMidiChanges();

	List<String> transmitters = new ArrayList<>();
	List<String> receivers = new ArrayList<>();
	List<String> sequencers = new ArrayList<>();
	List<String> synthesizers = new ArrayList<>();

	System.out.println("Getting MIDI devices...");
	MidiDevice.Info[] infos = getWorkingDeviceInfo();

	for (MidiDevice.Info dev : infos) {
	    String id = dev.getName();

	    if (dev instanceof CoreMidiDeviceInfo) {
		CoreMidiDeviceInfo cmDev = (CoreMidiDeviceInfo) dev;
		id = String.format("%s - %s", cmDev.getDeviceName(), cmDev.getEndPointName());
		System.out.println(id);
	    } else {
		System.out.println(dev.toString());
	    }
	    System.out.printf("  name: %s%n", dev.getName());
	    System.out.printf("  description: %s%n", dev.getDescription());
	    System.out.printf("  vendor: %s%n", dev.getVendor());
	    System.out.printf("  version: %s%n", dev.getVersion());

	    MidiDevice md = MidiSystem.getMidiDevice(dev);
	    if (md.getMaxTransmitters() > 0 || md.getMaxTransmitters() == -1) {
		transmitters.add(id);
	    }
	    if (md.getMaxReceivers() > 0 || md.getMaxReceivers() == -1) {
		receivers.add(id);
	    }
	    if (md instanceof Sequencer) {
		sequencers.add(id);
	    }
	    if (md instanceof Synthesizer) {
		synthesizers.add(id);
	    }
	}

	System.out.println();

	System.out.println("Transmitters:");
	for (String transmitter : transmitters) {
	    System.out.printf("  %s%n", transmitter);
	}
	System.out.println("Receivers:");
	for (String receiver : receivers) {
	    System.out.printf("  %s%n", receiver);
	}
	System.out.println("Sequencers:");
	for (String sequencer : sequencers) {
	    System.out.printf("  %s%n", sequencer);
	}
	System.out.println("Synthesizers:");
	for (String synthesizer : synthesizers) {
	    System.out.printf("  %s%n", synthesizer);
	}
    }
}
