package org.randomcoder.midi.mac.corefoundation;

public enum CFRunLoopRunResult {
	CFRunLoopRunUnknown(-1),
	CFRunLoopRunFinished(1),
	CFRunLoopRunStopped(2),
	CFRunLoopRunTimedOut(3),
	CFRunLoopRunHandledSource(4);

	private final int code;

	private CFRunLoopRunResult(int code) {
		this.code = code;
	}

	public int code() {
		return code;
	};

	public static CFRunLoopRunResult of(int code) {
		for (CFRunLoopRunResult value : values()) {
			if (value.code == code) {
				return value;
			}
		}
		return CFRunLoopRunUnknown;
	}
}
