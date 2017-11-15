package org.randomcoder.midi.coremidi;

import java.util.Arrays;

public enum CoreMidiError {

    kNoError(0, "Success"), kUnknownError(-65536, "Unknown error"), kMIDIInvalidClient(-10830,
	    "An invalid MIDIClientRef was passed."), kMIDIInvalidPort(-10831,
		    "An invalid MIDIPortRef was passed."), kMIDIWrongEndpointType(-10832,
			    "A source endpoint was passed to a function expecting a destination, or vice versa."), kMIDINoConnection(
				    -10833, "Attempt to close a non-existant connection."), kMIDIUnknownEndpoint(-10834,
					    "An invalid MIDIEndpointRef was passed."), kMIDIUnknownProperty(-10835,
						    "Attempt to query a property not set on the object."), kMIDIWrongPropertyType(
							    -10836,
							    "Attempt to set a property with a value not of the correct type."), kMIDINoCurrentSetup(
								    -10837,
								    "Internal error; there is no current MIDI setup object."), kMIDIMessageSendErr(
									    -10838,
									    "Communication with MIDIServer failed."), kMIDIServerStartErr(
										    -10839,
										    "Unable to start MIDIServer."), kMIDISetupFormatErr(
											    -10840,
											    "Unable to read the saved state."), kMIDIWrongThread(
												    -10841,
												    "A driver is calling a non-I/O function in the server from a thread other than the server's main thread."), kMIDIObjectNotFound(
													    -10842,
													    "The requested object does not exist."), kMIDIIDNotUnique(
														    -10843,
														    "Attempt to set a non-unique kMIDIPropertyUniqueID on an object."), kMIDINotPermitted(
															    -10844,
															    "Operation not permitted.");

    private final int errorCode;
    private final String description;

    CoreMidiError(final int errorCode, String description) {
	this.errorCode = errorCode;
	this.description = description;
    }

    public int errorCode() {
	return errorCode;
    }

    public String description() {
	return description;
    }

    public static CoreMidiError byErrorCode(final int intValue) {
	return Arrays.stream(CoreMidiError.values())
		.filter(e -> e.errorCode == intValue)
		.findFirst()
		.orElse(kUnknownError);
    }

}
