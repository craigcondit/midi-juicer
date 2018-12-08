package org.randomcoder.midi.mac.system;

import com.sun.jna.Native;

public class DirectSystemPeer implements SystemPeer {

  static {
    Native.register(SystemServiceFactory.LIBRARY_NAME);
  }

  @Override public native long mach_absolute_time();

  @Override public native long mach_approximate_time();

  @Override public native long mach_continuous_time();

  @Override public native long mach_continuous_approximate_time();

  @Override public native int mach_timebase_info(MachTimebaseInfo info);

  @Override public native int mach_wait_until(long deadline);
}
