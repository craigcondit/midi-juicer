package org.randomcoder.midi.experimental;

import java.util.*;
import java.util.stream.Collectors;

import javax.sound.midi.*;
import javax.sound.midi.Sequencer.SyncMode;

import org.jooq.lambda.Unchecked;

import uk.co.xfactorylibrarians.coremidi4j.CoreMidiDeviceProvider;

/**
 * Output a MIDI clock signal to every device which is capable of receiving
 * MIDI.
 */
public class MidiClock {

    public static void main(String[] args) throws Exception {
	System.out.println("Looking for MIDI devices...");

	List<MidiDevice> rxDevices = Arrays
		.stream(CoreMidiDeviceProvider.getMidiDeviceInfo())
		.filter(i -> i.getVendor().equals("Arturia"))
		.map(Unchecked.function(MidiSystem::getMidiDevice))
		.filter(d -> d.getMaxReceivers() == -1 || d.getMaxReceivers() > 0)
		.collect(Collectors.toList());

	rxDevices.stream().forEach(Unchecked.consumer(MidiDevice::open));

	Sequencer seq = MidiSystem.getSequencer(false);
	seq.open();

	System.out.printf("Opened sequencer %s with %d max transmitters.%n", seq.getDeviceInfo().getName(),
		seq.getMaxTransmitters());

	seq.setMasterSyncMode(SyncMode.INTERNAL_CLOCK);
	seq.setSlaveSyncMode(SyncMode.MIDI_SYNC);
	seq.setTempoInBPM(180.0f); // 120 bpm
	seq.setLoopCount(-1);
	
	System.out.printf("Located %d receive-capable MIDI devices.%n", rxDevices.size());

	List<Receiver> receivers = rxDevices
		.stream()
		.map(Unchecked.function(MidiDevice::getReceiver))
		.collect(Collectors.toList());

	System.out.printf("Initialized %d receivers.%n", receivers.size());

	List<Transmitter> transmitters = new ArrayList<>(receivers.size());
	for (Receiver r : receivers) {
	    Transmitter t = seq.getTransmitter();
	    transmitters.add(t);
	    t.setReceiver(r);
	}

	// add a new one
	seq.getTransmitter().setReceiver(new MidiDeviceReceiver() {

	    @Override
	    public void send(MidiMessage message, long timeStamp) {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < message.getLength(); i++) {
		    if (i > 0) {
			buf.append(" ");
		    }
		    buf.append(String.format("%02x", message.getMessage()[i]));
		}
		buf.append(" | ");
		for (int i = 0; i < message.getLength(); i++) {
		    if (i > 0) {
			buf.append(" ");
		    }
		    buf.append(String.format("%d", message.getMessage()[i] & 0xff));
		}
		System.out.printf("%d %s%n", timeStamp, buf.toString());
	    }

	    @Override
	    public void close() {
		// TODO Auto-generated method stub

	    }

	    @Override
	    public MidiDevice getMidiDevice() {
		// TODO Auto-generated method stub
		return null;
	    }
	});

	System.out.printf("Initialized %d transmitters.%n", receivers.size());

	Sequence sequence = new Sequence(Sequence.PPQ, 24);
	Track track = sequence.createTrack();
	seq.setSequence(sequence);

	// start sequencer
	seq.start();

	try {

	    System.out.println("Press a key to exit...");
	    System.in.read();
	    System.out.println("Shutting down");
	} finally {
	    rxDevices
		    .stream()
		    .map(MidiDevice::getReceivers)
		    .flatMap(List::stream)
		    .forEach(Receiver::close);

	    rxDevices.stream().forEach(Unchecked.consumer(MidiDevice::close));
	}
    }

}
