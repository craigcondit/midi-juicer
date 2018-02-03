package org.randomcoder.midi.mac.system;

import com.sun.jna.Library;

public interface SystemPeer extends Library {

    public long mach_absolute_time();

    public long mach_approximate_time();

    public long mach_continuous_time();

    public long mach_continuous_approximate_time();

    public int mach_timebase_info(MachTimebaseInfo info);

    public int mach_wait_until(long deadline);
}
