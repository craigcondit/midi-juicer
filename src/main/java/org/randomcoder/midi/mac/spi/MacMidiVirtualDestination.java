package org.randomcoder.midi.mac.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

public class MacMidiVirtualDestination extends AbstractMacMidiDevice {

	private final AtomicInteger transmitterIdGenerator = new AtomicInteger(0);

	private final List<Transmitter> transmitters = new CopyOnWriteArrayList<>();

	private boolean open = false;

	MacMidiVirtualDestination(String name, String vendor, String description, String version) {
		super(new MacMidiDeviceInfo(name, vendor, description, version, MacMidiDeviceType.VIRTUAL_DESTINATION));
	}

	@Override
	public int getMaxReceivers() {
		return 0;
	}

	@Override
	public Receiver getReceiver() throws MidiUnavailableException {
		throw new MidiUnavailableException();
	}

	@Override
	public List<Receiver> getReceivers() {
		return Collections.emptyList();
	}

	@Override
	public int getMaxTransmitters() {
		return 1;
	}

	@Override
	public Transmitter getTransmitter() throws MidiUnavailableException {
		if (transmitters.size() >= 1) {
			throw new MidiUnavailableException("Virtual destinations only support a single transmitter");
		}

		MacMidiVirtualDestinationTransmitter transmitter = new MacMidiVirtualDestinationTransmitter(this,
				transmitterIdGenerator.incrementAndGet());

		transmitters.add(transmitter);
		transmitter.open();
		return transmitter;
	}

	@Override
	public List<Transmitter> getTransmitters() {
		return Collections.unmodifiableList(new ArrayList<>(transmitters));
	}

	@Override
	public boolean isOpen() {
		return open;
	}

	@Override
	public void open() throws MidiUnavailableException {
		if (isOpen()) {
			return;
		}

		open = true;
	}

	@Override
	public void close() {
		if (!isOpen()) {
			return;
		}

		for (ListIterator<Transmitter> it = transmitters.listIterator(); it.hasPrevious();) {
			Transmitter transmitter = it.previous();
			transmitter.close();
			it.remove();
		}
		open = false;
	}

}
