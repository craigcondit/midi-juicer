package org.randomcoder.midi.mac.spi;

import org.randomcoder.midi.mac.RunLoop;
import org.randomcoder.midi.mac.coremidi.CoreMidi;

import javax.sound.midi.MidiDevice;
import java.util.concurrent.atomic.AtomicInteger;

abstract public class AbstractMacMidiDevice implements MidiDevice {
  private final MacMidiDeviceInfo info;
  private final int deviceRef;

  protected AbstractMacMidiDevice(MacMidiDeviceInfo info) {
    this.info = info;

    switch (info.getType()) {
    case SOURCE:
    case DESTINATION:
      AtomicInteger aDeviceRef = new AtomicInteger(-1);
      RunLoop.getDefault().orElseThrow(
          () -> new IllegalStateException("Default runloop not set"))
          .invokeAndWait(() -> {
            aDeviceRef.set(CoreMidi.getInstance()
                .getDeviceRefByUniqueID(info.getUniqueId()));
          });

      this.deviceRef = aDeviceRef.get();
      break;
    case VIRTUAL_SOURCE:
    case VIRTUAL_DESTINATION:
    default:
      deviceRef = Integer.MIN_VALUE;
    }
  }

  @Override public MacMidiDeviceInfo getDeviceInfo() {
    return info;
  }

  @Override public long getMicrosecondPosition() {
    return -1L;
  }

  public int getDeviceRef() {
    return deviceRef;
  }

  @Override public String toString() {
    return String
        .format("%s[name=%s, vendor=%s, description=%s, version=%s, type=%s]",
            getClass().getSimpleName(), info.getName(), info.getVendor(),
            info.getDescription(), info.getVersion(), info.getType());
  }
}
