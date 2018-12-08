package org.randomcoder.midi.mac.spi;

import org.randomcoder.midi.mac.coremidi.CoreMidi;

import javax.sound.midi.MidiDeviceReceiver;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;

public class MacMidiDestinationReceiver implements MidiDeviceReceiver {
  private final int id;
  private final MacMidiDestination destination;
  private final CoreMidi midi;

  private volatile Integer clientId;
  private volatile Integer outputPortId;
  private volatile boolean open = false;

  public MacMidiDestinationReceiver(MacMidiDestination destination, int id) {
    this.id = id;
    this.destination = destination;
    this.midi = CoreMidi.getInstance();
  }

  @Override public MacMidiDestination getMidiDevice() {
    return destination;
  }

  synchronized void open() throws MidiUnavailableException {
    if (open) {
      return;
    }

    String clientName = String.format("MacMidiDestinationReceiver:%d:%d",
        destination.getDeviceInfo().getUniqueId(), id);

    String outputPortName = String.format("MacMidiOutputPort:%d:%d",
        destination.getDeviceInfo().getUniqueId(), id);

    destination.open();

    clientId = midi.createClient(clientName, (m, t) -> {
    });
    outputPortId = midi.createOutputPort(outputPortName, clientId);

    open = true;
  }

  @Override public synchronized void close() {
    if (outputPortId != null) {
      midi.closeOutputPort(outputPortId);
      outputPortId = null;
    }

    if (clientId != null) {
      midi.closeClient(clientId);
      clientId = null;
    }

    open = false;
  }

  @Override public void send(MidiMessage message, long timestamp) {
    if (!open) {
      throw new IllegalStateException("destination is closed");
    }

    midi.sendMidi(message, timestamp, outputPortId, destination.getDeviceRef());
  }

}
