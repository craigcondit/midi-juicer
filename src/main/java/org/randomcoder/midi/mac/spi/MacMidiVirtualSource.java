package org.randomcoder.midi.mac.spi;

import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class MacMidiVirtualSource extends AbstractMacMidiDevice {

  private final AtomicInteger receiverIdGenerator = new AtomicInteger(0);

  private final List<Receiver> receivers = new CopyOnWriteArrayList<>();

  private boolean open = false;

  public MacMidiVirtualSource(String name, String vendor, String description,
      String version) {
    super(new MacMidiDeviceInfo(name, vendor, description, version,
        MacMidiDeviceType.VIRTUAL_SOURCE));
  }

  @Override public int getMaxReceivers() {
    return 1;
  }

  @Override public Receiver getReceiver() throws MidiUnavailableException {
    if (receivers.size() >= 1) {
      throw new MidiUnavailableException(
          "Virtual sources only support a single receiver");
    }

    MacMidiVirtualSourceReceiver receiver =
        new MacMidiVirtualSourceReceiver(this,
            receiverIdGenerator.incrementAndGet());

    receivers.add(receiver);
    receiver.open();
    return receiver;
  }

  @Override public List<Receiver> getReceivers() {
    return Collections.unmodifiableList(new ArrayList<>(receivers));
  }

  @Override public int getMaxTransmitters() {
    return 0;
  }

  @Override public Transmitter getTransmitter()
      throws MidiUnavailableException {
    throw new MidiUnavailableException("No transmitters available");
  }

  @Override public List<Transmitter> getTransmitters() {
    return Collections.emptyList();
  }

  @Override public synchronized boolean isOpen() {
    return open;
  }

  @Override public synchronized void open() throws MidiUnavailableException {
    if (isOpen()) {
      return;
    }

    open = true;
  }

  @Override public synchronized void close() {
    if (!isOpen()) {
      return;
    }

    for (ListIterator<Receiver> it = receivers.listIterator(); it
        .hasPrevious(); ) {
      Receiver receiver = it.previous();
      receiver.close();
      it.remove();
    }
    open = false;
  }

}
