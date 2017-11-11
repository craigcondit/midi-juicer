package org.randomcoder.midi.coremidi;

import static org.junit.Assert.assertSame;

import org.junit.Test;

public class CoreMidiErrorTest {

	@Test
	public void byErrorCodeMatchesExistingValues() {
		for (CoreMidiError error : CoreMidiError.values()) {
			assertSame(error, CoreMidiError.byErrorCode(error.errorCode()));
		}
	}

	@Test
	public void byErrorCodeOSStatusMatchesExistingValues() {
		for (CoreMidiError error : CoreMidiError.values()) {
			int status = error.errorCode();
			assertSame(error, CoreMidiError.byErrorCode(status));
		}
	}

	@Test
	public void byErrorCodeReturnsUnknownOnInvalidCode() {
		assertSame(CoreMidiError.kUnknownError, CoreMidiError.byErrorCode(Integer.MIN_VALUE));
	}

	@Test
	public void byErrorCodeOSStatusReturnsUnknownOnInvalidCode() {
		assertSame(CoreMidiError.kUnknownError, CoreMidiError.byErrorCode(Integer.MIN_VALUE));
	}

}
