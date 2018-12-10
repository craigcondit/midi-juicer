package org.randomcoder.midi.samples.mpx550;

import org.randomcoder.midi.mac.MacMidi;
import org.randomcoder.midi.mac.RunLoop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

public class Mpx550SysEx {

  private static final Logger LOG = LoggerFactory.getLogger(Mpx550SysEx.class);

  private static final byte[] IDENTITY_REQUEST_BYTES = new byte[] {
      (byte) SysexMessage.SYSTEM_EXCLUSIVE,
      0x7e, 0x7f, 0x06, 0x01,
      (byte) ShortMessage.END_OF_EXCLUSIVE
  };

  private static final SysexMessage IDENTITY_REQUEST;

  static {
    try {
      IDENTITY_REQUEST = new SysexMessage(
          IDENTITY_REQUEST_BYTES, IDENTITY_REQUEST_BYTES.length);
    } catch (InvalidMidiDataException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  public static void main(String[] args) throws Exception {

    try (var rl = RunLoop.spawn(true)) {
      if (MacMidi.available()) {
        MacMidi.init();

        MacMidi.addSetupChangedListener(
            e -> LOG.info("MIDI setup changed: {}", e));
      }

      dumpDevices();

      LOG.info("Ready");

      var input = getInput().orElseThrow(
          () -> new IllegalStateException("Couldn't find input device"));

      var output = getOutput().orElseThrow(
          () -> new IllegalStateException("Couldn't find output device"));

      try (output; var receiver = output.getReceiver()) {
        LOG.debug("Opened receiver for device: {}", output);
        output.open();

        try (input; var transmitter = input.getTransmitter()) {
          LOG.debug("Opened transmitter for device: {}", input);
          input.open();

          var counter = new AtomicLong(0L);

          // wire up a logging receiver
          var reader = new PluggableReceiver((m, t) -> {
            LOG.debug("Input:");
            dumpMessage(m);
            counter.incrementAndGet();
          });

          transmitter.setReceiver(reader);

          // write a SysEx identity request message
          LOG.debug("Sending SysEx identity request...");
          long expectedValue = counter.get() + 1L;
          LOG.debug("Output:");
          dumpMessage(IDENTITY_REQUEST);
          receiver.send(IDENTITY_REQUEST, output.getMicrosecondPosition());
          while (counter.get() != expectedValue) {
            Thread.onSpinWait();
            Thread.sleep(10L);
          }

          // dump all settings
          for (var param : Mpx550Parameter.values()) {
            LOG.debug("Querying for {}", param);
            expectedValue = counter.get() + 1L;
            MidiMessage msg = param.buildQuery();
            LOG.debug("Output:");
            dumpMessage(msg);
            receiver.send(msg, output.getMicrosecondPosition());
            while (counter.get() != expectedValue) {
              Thread.onSpinWait();
              Thread.sleep(10L);
            }
          }
          LOG.debug("Queried all parameters.");
        }
      }

    }
  }

  private static void dumpDevices() {
    Arrays
        .stream(MidiSystem.getMidiDeviceInfo())
        .forEach(d -> LOG.info(
            "Found device: {}, vendor={}, description={}",
            d.getName(),
            d.getVendor(),
            d.getDescription()));
  }

  private static Optional<MidiDevice> getInput() {
    return Arrays
        .stream(MidiSystem.getMidiDeviceInfo())
        .filter(MacMidi::isMacMidiDevice)
        .filter(di -> "MPX550".equals(di.getName()))
        .map(di -> {
          try {
            return MidiSystem.getMidiDevice(di);
          } catch (Exception e) {
            return null;
          }
        })
        .filter(Objects::nonNull)
        .filter(d -> d.getMaxTransmitters() != 0)
        .findFirst();
  }

  private static Optional<MidiDevice> getOutput() {
    return Arrays
        .stream(MidiSystem.getMidiDeviceInfo())
        .filter(MacMidi::isMacMidiDevice)
        .filter(di -> "MPX550".equals(di.getName()))
        .map(di -> {
          try {
            return MidiSystem.getMidiDevice(di);
          } catch (Exception e) {
            return null;
          }
        })
        .filter(Objects::nonNull)
        .filter(d -> d.getMaxReceivers() != 0)
        .findFirst();
  }

  private static void reset(Receiver rx) {
    try {
      System.out.println("ALL NOTES OFF");
      rx.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, 0, 123, 0),
          -1L); // all
      // notes
      // off
      System.out.println("DAMPER OFF");
      rx.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, 0, 64, 0),
          -1L); // damper
      // off
      System.out.println("PORTAMENTO OFF");
      rx.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, 0, 65, 0),
          -1L); // portamento
      // off
      System.out.println("SUSTENUTO OFF");
      rx.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, 0, 66, 0),
          -1L); // sustenuto
      // off
    } catch (InvalidMidiDataException e) {
      throw new RuntimeException(e);
    }
  }

  private static void dumpMessage(MidiMessage message) {
    LOG.debug(
        "Midi message: {} [len={}]",
        message.getClass().getSimpleName(),
        message.getLength());
    var buf = new StringBuilder();
    for (int i = 0; i < message.getLength(); i++) {
      buf.append(String.format("  %02x", message.getMessage()[i]));
    }
    LOG.debug("  data (hex): {}", buf.toString());
    buf.setLength(0);
    for (int i = 0; i < message.getLength(); i++) {
      buf.append(String.format(" %3d", message.getMessage()[i] & 0xff));
    }
    LOG.debug("  data (dec): {}", buf.toString());
  }

  private static class PluggableReceiver implements Receiver {

    private final BiConsumer<MidiMessage, Long> consumer;

    PluggableReceiver(BiConsumer<MidiMessage, Long> consumer) {
      this.consumer = consumer;
    }

    @Override
    public void send(MidiMessage message, long timeStamp) {
      consumer.accept(message, timeStamp);
    }

    @Override
    public void close() {
    }

  }

}
