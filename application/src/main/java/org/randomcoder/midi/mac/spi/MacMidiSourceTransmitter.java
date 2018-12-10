package org.randomcoder.midi.mac.spi;

import com.sun.jna.Pointer;
import org.randomcoder.midi.mac.coremidi.CoreMidi;
import org.randomcoder.midi.mac.coremidi.MIDIPacketList;

import javax.sound.midi.MidiDeviceTransmitter;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import java.util.concurrent.atomic.AtomicReference;

public class MacMidiSourceTransmitter implements MidiDeviceTransmitter {

  private final int id;
  private final MacMidiSource source;
  private final CoreMidi midi;
  private final AtomicReference<Receiver> receiverHolder =
      new AtomicReference<>(null);

  private final MidiMessageConverter.ReceiveState receiveState =
      new MidiMessageConverter.ReceiveState();

  private volatile Integer clientId;
  private volatile Integer inputPortId;
  private volatile Pointer connRef;
  private volatile boolean open = false;

  private final MidiMessageConverter.ReceiveState state =
      new MidiMessageConverter.ReceiveState();

  public MacMidiSourceTransmitter(MacMidiSource source, int id) {
    this.id = id;
    this.source = source;
    this.midi = CoreMidi.getInstance();
  }

  synchronized void open() throws MidiUnavailableException {
    if (open) {
      return;
    }

    String clientName = String.format("MacMidiSourceTransmitter:%d:%d",
        source.getDeviceInfo().getUniqueId(), id);

    String inputPortName = String
        .format("MacMidiInputPort:%d:%d", source.getDeviceInfo().getUniqueId(),
            id);

    source.open();

    clientId = midi.createClient(clientName, (m, t) -> {
    });

    inputPortId = midi.createInputPort(
        inputPortName, clientId, this::handleMidi);
    connRef = midi.connectSource(inputPortId, source.getDeviceRef());

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

    if (connRef != null) {
      if (inputPortId != null) {
        midi.disconnectSource(inputPortId, source.getDeviceRef(), connRef);
      }
      connRef = null;
    }
    if (inputPortId != null) {
      midi.closeInputPort(inputPortId);
      inputPortId = null;
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

  @Override public MacMidiSource getMidiDevice() {
    return source;
  }

}
