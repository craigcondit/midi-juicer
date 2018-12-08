package org.randomcoder.midi.samples;

import org.randomcoder.midi.mac.MacMidi;
import org.randomcoder.midi.mac.RunLoop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

public class MidiVirtualSource {

  private static final Logger LOG =
      LoggerFactory.getLogger(MidiVirtualSource.class);

  public static void main(String[] args) throws Exception {

    try (RunLoop rl = RunLoop.spawn(true)) {
      if (!MacMidi.available()) {
        LOG.error("MacMidi required");
        return;
      }

      MacMidi.init();

      MacMidi.addSetupChangedListener(e -> {
        LOG.info("MIDI setup changed: {}", e);
        rl.invokeAndWait(() -> dumpDevices(MidiSystem.getMidiDeviceInfo()));
      });

      LOG.info("MacMidi setup complete");

      try (MidiDevice device = MacMidi
          .createVirtualSource("Input 1", "RandomCoder", "RandomCoder Input 1",
              "1.0.0"); Receiver r = device.getReceiver()) {

        LOG.info("Opened receiver for device: {}", device);

        while (true) {
          Thread.sleep(1000L);
          r.send(new ShortMessage(ShortMessage.NOTE_ON, 60, 127), -1L);
          Thread.sleep(1000L);
          r.send(new ShortMessage(ShortMessage.NOTE_OFF, 60, 0), -1L);
        }
      }
    }
  }

  private static void dumpDevices(MidiDevice.Info[] infos) {
    for (MidiDevice.Info info : infos) {
      LOG.info("Found MIDI device info {}: {}", info,
          info.getClass().getName());

      try {
        MidiDevice dev = MidiSystem.getMidiDevice(info);
        LOG.info("  Device: {}", dev);
      } catch (MidiUnavailableException e) {
        e.printStackTrace();
      }
    }
  }
}
