package org.randomcoder.midi.system;

import com.sun.jna.Native;

public class DirectSystemPeer implements SystemPeer {

	static {
		Native.register(SystemServiceFactory.LIBRARY_NAME);
	}

	public native long mach_absolute_time();

	public native long mach_approximate_time();

	public native long mach_continuous_time();

	public native long mach_continuous_approximate_time();

	public native int mach_timebase_info(MachTimebaseInfo info);

	public native int mach_wait_until(long deadline);
}
