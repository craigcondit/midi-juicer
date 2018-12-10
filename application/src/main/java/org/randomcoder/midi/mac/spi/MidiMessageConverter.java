package org.randomcoder.midi.mac.spi;

import org.randomcoder.midi.mac.coremidi.MIDIPacket;
import org.randomcoder.midi.mac.coremidi.MIDIPacketList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MidiMessageConverter {

  private static final byte[] EMPTY = new byte[0];

  private static final Logger LOG =
      LoggerFactory.getLogger(MidiMessageConverter.class);

  private enum ReceiveMode {
    NORMAL, SYSEX;
  }

  public static class ReceiveState {
    private byte[] buffer = EMPTY;
    private ReceiveMode mode = ReceiveMode.NORMAL;
  }

  private static boolean validStatusByte(byte b) {
    return ((b & 0xff) > 0x80) && ((b & 0xf0) != 0xf0);
  }

  private static boolean sysexStart(byte b) {
    return b == (byte) SysexMessage.SYSTEM_EXCLUSIVE;
  }

  private static boolean sysexEnd(byte b) {
    return b == (byte) SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE;
  }

  public static List<MidiMessage> coreMidiToJava(
      ReceiveState state, MIDIPacketList pList) {

    // log packets
    if (LOG.isTraceEnabled()) {
      LOG.trace("Packet list: {}", pList);
      StringBuilder buf = new StringBuilder();
      for (int i = 0; i < pList.getLength(); i++) {
        MIDIPacket packet = pList.getPackets().get(i);
        LOG.trace("Packet: {}", packet);

        buf.setLength(0);
        for (short j = 0; j < packet.getLength(); j++) {
          buf.append(String.format("  %02x", packet.getData()[j]));
        }
        LOG.trace("  data (hex): {}", buf.toString());

        buf.setLength(0);
        for (short j = 0; j < packet.getLength(); j++) {
          buf.append(String.format(" %3d", packet.getData()[j] & 0xff));
        }
        LOG.trace("  data (dec): {}", buf.toString());
      }
    }

    // convert to Java MidiMessage
    List<MidiMessage> messages = new ArrayList<>();

    for (int i = 0; i < pList.getLength(); i++) {
      LOG.trace("Parsing packet {} of {}", i + 1, pList.getLength());

      MIDIPacket packet = pList.getPackets().get(i);
      if (packet.getLength() == 0) {
        LOG.trace("Got empty packet");
        continue;
      }

      MidiMessage message = null;
      byte first = packet.getData()[0];
      byte last = packet.getData()[packet.getLength() - 1];

      try {
        switch (state.mode) {
        case NORMAL: {
          if (validStatusByte(first)) {
            // start of normal message
            if (packet.getLength() == 1) {
              LOG.trace("Got short message of length 1");
              message = new ShortMessage(packet.getData()[0] & 0xff);
            } else if (packet.getLength() == 2) {
              LOG.trace("Got short message of length 2");
              message = new ShortMessage(
                  packet.getData()[0] & 0xff,
                  packet.getData()[1] & 0xff, 0);
            } else if (packet.getLength() == 3) {
              LOG.trace("Got short message of length 3");
              message = new ShortMessage(
                  packet.getData()[0] & 0xff,
                  packet.getData()[1] & 0xff,
                  packet.getData()[2] & 0xff);
            } else {
              LOG.warn(
                  "Got invalid short message of length {}",
                  packet.getLength());
              continue;
            }
          } else if (sysexStart(first)) {
            // start of sysex message
            if (!sysexEnd(last)) {
              // packet is incomplete, copy into state
              LOG.trace(
                  "Got incomplete sysex message of length {}, adding to buffer",
                  packet.getLength());
              state.buffer =
                  Arrays.copyOf(packet.getData(), packet.getLength());
              state.mode = ReceiveMode.SYSEX;
              continue;
            }
            LOG.trace(
                "Got complete sysex message of length {}",
                packet.getLength());
            message = new SysexMessage(packet.getData(), packet.getLength());
          } else {
            LOG.warn("Got seemingly bad packet");
            continue;
          }
        }
        break;
        case SYSEX: {
          state.buffer = Arrays
              .copyOf(state.buffer, state.buffer.length + packet.getLength());
          System.arraycopy(
              packet.getData(),
              0,
              state.buffer,
              state.buffer.length - packet.getLength(),
              packet.getLength());
          if (!sysexEnd(last)) {
            LOG.trace(
                "Got additional incomplete sysex message of length {}, adding to buffer",
                packet.getLength());
            continue;
          }
          LOG.trace(
              "Got final complete sysex message of length {}, completing",
              packet.getLength());
          message = new SysexMessage(state.buffer, state.buffer.length);
          state.buffer = EMPTY;
          state.mode = ReceiveMode.NORMAL;
        }
        break;
        }

        messages.add(message);

      } catch (InvalidMidiDataException e) {
        LOG.warn("Invalid MIDI packet received: {}", e.getMessage());
        continue;
      }
    }

    return messages;
  }

}
