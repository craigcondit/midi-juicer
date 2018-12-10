package org.randomcoder.midi.mac.spi;

import com.sun.jna.Pointer;
import org.randomcoder.midi.mac.coremidi.CoreMidi;
import org.randomcoder.midi.mac.coremidi.MIDIPacketList;

import javax.sound.midi.MidiDeviceTransmitter;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import java.util.concurrent.atomic.AtomicReference;

public class MacMidiVirtualDestinationTransmitter
    implements MidiDeviceTransmitter {

  private final int id;
  private final MacMidiVirtualDestination destination;
  private final CoreMidi midi;
  private final AtomicReference<Receiver> receiverHolder =
      new AtomicReference<>(null);

  private final MidiMessageConverter.ReceiveState receiveState =
      new MidiMessageConverter.ReceiveState();

  private volatile Integer clientId;
  private volatile Integer destId;
  private volatile boolean open = false;

  public MacMidiVirtualDestinationTransmitter(
      MacMidiVirtualDestination destination, int id) {
    this.id = id;
    this.destination = destination;
    this.midi = CoreMidi.getInstance();
  }

  synchronized void open() throws MidiUnavailableException {
    if (open) {
      return;
    }

    String clientName = String
        .format("MacMidiVirtualDestinationTransmitter:%d:%d",
            destination.getDeviceInfo().getUniqueId(), id);

    String destName = destination.getDeviceInfo().getDescription();

    destination.open();

    clientId = midi.createClient(clientName, (m, t) -> {
    });
    destId = midi.createDestination(destName, clientId, this::handleMidi);

    open = true;
  }

  private void handleMidi(
      Pointer pktlist,
      Pointer readProcRefCon,
      Pointer srcConnRefCon) {

    Receiver receiver = receiverHolder.get();

    // short-circuit out if receiver is not set or transmitter inactive
    if (receiver == null || !open) {
      return;
    }

    // go through packets
    MIDIPacketList pList = new MIDIPacketList(pktlist, 0);

    for (MidiMessage message : MidiMessageConverter.coreMidiToJava(
        receiveState, pList)) {
      receiver.send(message, -1L);
    }
  }

  @Override public void close() {
    receiverHolder.set(null);

    if (destId != null) {
      midi.closeDestination(destId);
      destId = null;
    }

    if (clientId != null) {
      midi.closeClient(clientId);
      clientId = null;
    }

    open = false;
  }

  @Override public Receiver getReceiver() {
    return receiverHolder.get();
  }

  @Override public void setReceiver(Receiver receiver) {
    receiverHolder.set(receiver);
  }

  @Override public MacMidiVirtualDestination getMidiDevice() {
    return destination;
  }

}
