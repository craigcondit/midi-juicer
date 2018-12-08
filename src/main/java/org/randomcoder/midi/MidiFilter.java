package org.randomcoder.midi;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;

@FunctionalInterface public interface MidiFilter {

  public MidiMessage[] handle(MidiMessage message, long timeStamp);

  public static Receiver toReceiver(final Receiver dest,
      final MidiFilter filter) {
    return new Receiver() {

      @Override
      public void send(final MidiMessage message, final long timeStamp) {
        MidiMessage[] msgs = filter.handle(message, timeStamp);
        if (msgs != null) {
          for (MidiMessage m : msgs) {
            dest.send(m, timeStamp);
          }
        }
      }

      @Override public void close() {
        dest.close();
      }
    };
  }

}
