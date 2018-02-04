package org.randomcoder.midi.mac.spi;

import javax.sound.midi.MidiDeviceReceiver;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;

import org.randomcoder.midi.mac.coremidi.CoreMidi;
import org.randomcoder.midi.mac.coremidi.MIDINotification;

public class MacMidiDestinationReceiver implements MidiDeviceReceiver {
	private final int id;
	private final MacMidiDestination destination;
	private final CoreMidi midi;

	private volatile MacRunLoopThread runLoopThread;
	private volatile Integer clientId;
	private volatile Integer outputPortId;
	private volatile boolean open = false;

	public MacMidiDestinationReceiver(MacMidiDestination destination, int id) {
		this.id = id;
		this.destination = destination;
		this.midi = CoreMidi.getInstance();
	}

	@Override
	public MacMidiDestination getMidiDevice() {
		return destination;
	}

	private void handleMidiNotification(MIDINotification event) {
		System.out.println(event);
	}

	synchronized void open() throws MidiUnavailableException {
		if (open) {
			return;
		}

		String rlName = String.format("runloop-receiver-%d-%d",
				destination.getDeviceInfo().getUniqueId(), id);

		String clientName = String.format("receiver-%d-%d",
				destination.getDeviceInfo().getUniqueId(), id);

		String outputPortName = String.format("output-%d-%d",
				destination.getDeviceInfo().getUniqueId(), id);

		destination.open();
		runLoopThread = midi.createRunLoopThread(rlName);
		runLoopThread.start();

		clientId = midi.createClient(clientName, runLoopThread, this::handleMidiNotification);
		outputPortId = midi.createOutputPort(outputPortName, clientId);

		open = true;
	}

	@Override
	public synchronized void close() {
		if (outputPortId != null) {
			midi.closeOutputPort(outputPortId);
			outputPortId = null;
		}

		if (clientId != null) {
			midi.closeClient(clientId);
			clientId = null;
		}

		if (runLoopThread != null) {
			try {
				runLoopThread.close();
			} catch (InterruptedException ignored) {
			}
			runLoopThread = null;
		}

		open = false;
	}

	@Override
	public void send(MidiMessage message, long timestamp) {
		if (!open) {
			throw new IllegalStateException("destination is closed");
		}

		midi.sendMidi(message, timestamp, outputPortId, destination.getDeviceRef());
	}

}
