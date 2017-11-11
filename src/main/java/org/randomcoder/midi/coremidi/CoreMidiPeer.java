package org.randomcoder.midi.coremidi;

import org.randomcoder.midi.corefoundation.CFStringRef;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public interface CoreMidiPeer extends Library {

	public interface MIDINotifyProc extends Callback {
		public void invoke(Pointer message, Pointer refCon);
	}

	public interface MIDIReadProc extends Callback {
		public void invoke(Pointer pktlist, Pointer readProcRefCon, Pointer srcConnRefCon);
	}

	public int MIDIClientCreate(
			CFStringRef name, MIDINotifyProc notifyProc, Pointer notifyRefCon, IntByReference outClient);

	public int MIDIClientDispose(int client);

	public int MIDIInputPortCreate(
			int client, CFStringRef portName, MIDIReadProc readProc, Pointer refCon, IntByReference outPort);

	public int MIDIDestinationCreate(
			int client, CFStringRef name, MIDIReadProc readProc, Pointer refCon, IntByReference outDest);

	public int MIDIGetNumberOfDevices();

	public int MIDIGetDevice(int deviceIndex0);

	public int MIDIGetNumberOfSources();

	public int MIDIGetSource(int sourceIndex0);

	public int MIDIGetNumberOfDestinations();

	public int MIDIGetDestination(int destIndex0);

	public int MIDIObjectGetIntegerProperty(int obj, CFStringRef propertyID, Pointer outValue);

	public int MIDIObjectGetStringProperty(int obj, CFStringRef propertyID, Pointer str);

}
