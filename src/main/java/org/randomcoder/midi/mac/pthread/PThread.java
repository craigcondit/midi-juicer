package org.randomcoder.midi.mac.pthread;

public class PThread {
	private static PThread INSTANCE = new PThread();

	public static PThread getInstance() {
		return INSTANCE;
	}

	static void setInstance(PThread instance) {
		INSTANCE = instance;
	}

	PThread() {
	}

	PThreadPeer peer() {
		return PThreadServiceFactory.getPeer();
	}

	public boolean isMainThread() {
		return peer().pthread_main_np() != 0;
	}
}
