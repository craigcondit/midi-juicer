package org.randomcoder.midi.experimental;

import org.randomcoder.midi.system.MachTimebaseInfo;
import org.randomcoder.midi.system.SystemPeer;
import org.randomcoder.midi.system.SystemServiceFactory;

public class PrintMachAbsoluteTime {

	public static void main(String[] args) {
		SystemPeer sp = SystemServiceFactory.getPeer();

		for (int i = 0; i < 10; i++) {
			System.out.printf(
					"mach_absolute_time: %d  mach_continuous_time: %d  mach_approximate_time: %d  mach_continuous_approximate_time: %d%n",
					sp.mach_absolute_time(), sp.mach_continuous_time(), sp.mach_approximate_time(),
					sp.mach_continuous_approximate_time());
		}
		MachTimebaseInfo info = new MachTimebaseInfo();
		int result = sp.mach_timebase_info(info);
		info.read();
		System.out.printf("%d: %d / %d%n", result, info.numer, info.denom);
	}
}
