package org.randomcoder.midi.mac.spi;

import org.randomcoder.midi.mac.coremidi.CoreMidi;

import javax.sound.midi.MidiDeviceReceiver;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;

public class MacMidiVirtualSourceReceiver implements MidiDeviceReceiver {
  private final int id;
  private final MacMidiVirtualSource source;
  private final CoreMidi midi;

  private volatile Integer clientId;
  private volatile Integer sourceId;
  private volatile boolean open = false;

  public MacMidiVirtualSourceReceiver(MacMidiVirtualSource source, int id) {
    this.id = id;
    this.source = source;
    this.midi = CoreMidi.getInstance();
  }

  @Override public MacMidiVirtualSource getMidiDevice() {
    return source;
  }

  synchronized void open() throws MidiUnavailableException {
    if (open) {
      return;
    }

    String clientName = String.format("MacMidiVirtualSourceReceiver:%d:%d",
        source.getDeviceInfo().getUniqueId(), id);

    String destName = source.getDeviceInfo().getDescription();

    source.open();

    clientId = midi.createClient(clientName, (m, t) -> {
    });
    sourceId = midi.createSource(clientId, destName);

    open = true;
  }

  @Override public synchronized void close() {
    if (sourceId != null) {
      midi.closeSource(sourceId);
      sourceId = null;
    }

    if (clientId != null) {
      midi.closeClient(clientId);
      clientId = null;
    }

    open = false;
  }

  @Override public void send(MidiMessage message, long timestamp) {
    if (!open) {
      throw new IllegalStateException("receiver is closed");
    }

    midi.midiReceived(message, timestamp, sourceId);
  }

}
