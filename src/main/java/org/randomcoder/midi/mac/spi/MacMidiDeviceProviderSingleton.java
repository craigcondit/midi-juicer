package org.randomcoder.midi.mac.spi;

import org.randomcoder.midi.mac.MacMidi;
import org.randomcoder.midi.mac.RunLoop;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.spi.MidiDeviceProvider;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

public class MacMidiDeviceProviderSingleton extends MidiDeviceProvider {

  private final ConcurrentMap<Integer, MacMidiSource> sources =
      new ConcurrentHashMap<>();
  private final ConcurrentMap<Integer, MacMidiDestination> destinations =
      new ConcurrentHashMap<>();

  @Override public boolean isDeviceSupported(MidiDevice.Info info) {
    return info instanceof MacMidiDeviceInfo;
  }

  @Override public MidiDevice getDevice(MidiDevice.Info info) {
    if (!MacMidi.available()) {
      throw new UnsupportedOperationException(
          "MacMidi subsystem is unavailable");
    }

    MacMidiDeviceInfo minfo = (MacMidiDeviceInfo) info;
    Integer uniqueId = minfo.getUniqueId();

    switch (minfo.getType()) {
    case SOURCE:
      return sources.compute(uniqueId,
          (k, v) -> (v == null) ? new MacMidiSource(minfo) : v);
    case DESTINATION:
      return destinations.compute(uniqueId,
          (k, v) -> (v == null) ? new MacMidiDestination(minfo) : v);
    default:
      throw new IllegalArgumentException(
          String.format("Unknown device type %s", minfo.getType()));
    }
  }

  @Override public MidiDevice.Info[] getDeviceInfo() {
    if (!MacMidi.available()) {
      return new MidiDevice.Info[0];
    }

    AtomicReference<MidiDevice.Info[]> result =
        new AtomicReference<>(new MidiDevice.Info[0]);
    RunLoop.getDefault()
        .orElseThrow(() -> new IllegalStateException("Default runloop not set"))
        .invokeAndWait(() -> {
          try {
            result.set(MacMidiDeviceInfo.getDeviceInfo());
          } catch (Exception e) {
            e.printStackTrace();
          }
        });

    return result.get();
  }

  public MidiDevice createVirtualSource(String name, String vendor,
      String description, String version) {
    AtomicReference<MacMidiVirtualSource> result = new AtomicReference<>(null);
    RunLoop.getDefault()
        .orElseThrow(() -> new IllegalStateException("Default runloop not set"))
        .invokeAndWait(() -> {
          result.set(
              new MacMidiVirtualSource(name, vendor, description, version));
        });

    return result.get();
  }

  public MidiDevice createVirtualDestination(String name, String vendor,
      String description, String version) {
    AtomicReference<MacMidiVirtualDestination> result =
        new AtomicReference<>(null);
    RunLoop.getDefault()
        .orElseThrow(() -> new IllegalStateException("Default runloop not set"))
        .invokeAndWait(() -> {
          result.set(new MacMidiVirtualDestination(name, vendor, description,
              version));
        });

    return result.get();
  }

}
