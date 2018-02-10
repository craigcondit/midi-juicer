package org.randomcoder.midi.samples;

import org.randomcoder.midi.mac.MacMidi;
import org.randomcoder.midi.mac.dispatch.Dispatch;

import com.sun.jna.Pointer;

public class MidiListener {

	public static void main(String[] args) throws Exception {

		System.out.println(Dispatch.getInstance().getMainQueue());

		Dispatch d = Dispatch.getInstance();
		Pointer mainQueue = d.getMainQueue();
		System.out.printf("Java main thread: %s%n", Thread.currentThread().getName());
		d.dispatchSyncFunction(mainQueue, mainQueue, c -> {
			System.out.printf("GCD main thread: %s%n", Thread.currentThread().getName());
		});

		if (MacMidi.isAvailable()) {
			MacMidi.init();

			MacMidi.addSetupChangedListener(e -> {
				System.out.println(e);
			});
		}

		try {
			while (true) {
				Thread.sleep(1000L);
				// cf.CFRunLoopRun();
			}
		} finally {
			MacMidi.shutdown();
		}
	}

}
