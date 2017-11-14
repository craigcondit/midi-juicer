package org.randomcoder.midi.coremidi;

import org.randomcoder.midi.corefoundation.CFStringRef;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class DirectCoreMidiPeer implements CoreMidiPeer {

	static {
		Native.register(CoreMidiServiceFactory.LIBRARY_NAME);
	}

	// MIDI Ports

	public native int MIDIInputPortCreate(
			int client,
			CFStringRef portName,
			MIDIReadProc readProc,
			Pointer refCon,
			IntByReference outPort);

	public native int MIDIOutputPortCreate(int client, CFStringRef portName, IntByReference outPort);

	public native int MIDIPortConnectSource(int port, int source, Pointer connRefCon);

	public native int MIDIPortDisconnectSource(int port, int source);

	public native int MIDIPortDispose(int port);

	// TODO MIDI Packet Lists

	// MIDIPacketListAdd
	// MIDIPacketListInit

	// MIDI Objects and Properties

	public native int MIDIObjectFindByUniqueID(
			int inUniqueID, IntByReference outObject, IntByReference outObjectType);

	public native int MIDIObjectGetIntegerProperty(int obj, CFStringRef propertyID, Pointer outValue);

	public native int MIDIObjectGetStringProperty(int obj, CFStringRef propertyID, Pointer str);

	public native int MIDIObjectRemoveProperty(int obj, CFStringRef propertyID);

	public native int MIDIObjectSetIntegerProperty(int obj, CFStringRef propertyID, int value);

	public native int MIDIObjectSetStringProperty(int obj, CFStringRef propertyID, CFStringRef str);

	// MIDI I/O

	public native int MIDIFlushOutput(int dest);

	public native int MIDIReceived(int src, Pointer pktlist);

	public native int MIDIRestart();

	public native int MIDISend(int port, int dest, Pointer pktlist);

	public native int MIDISendSysex(Pointer request);

	// MIDI External Devices

	public native int MIDIGetNumberOfExternalDevices();

	public native int MIDIGetExternalDevice(int deviceIndex0);

	// MIDI Entities

	public native int MIDIEntityGetDestination(int entity, int destIndex0);

	public native int MIDIEntityGetDevice(int inEntity, IntByReference outDevice);

	public native int MIDIEntityGetNumberOfDestinations(int entity);

	public native int MIDIEntityGetNumberOfSources(int entity);

	public native int MIDIEntityGetSource(int entity, int sourceIndex0);

	// MIDI Endpoints

	public native int MIDIDestinationCreate(
			int client,
			CFStringRef name,
			MIDIReadProc readProc,
			Pointer refCon,
			IntByReference outDest);

	public native int MIDIEndpointDispose(int endpt);

	public native int MIDIEndpointGetEntity(int inEndpoint, IntByReference outEntity);

	public native int MIDIGetDestination(int destIndex0);

	public native int MIDIGetNumberOfDestinations();

	public native int MIDIGetNumberOfSources();

	public native int MIDIGetSource(int sourceIndex0);

	public native int MIDISourceCreate(int client, CFStringRef name, IntByReference outSrc);

	// MIDI Devices

	public native int MIDIDeviceGetEntity(int device, int entityIndex0);

	public native int MIDIDeviceGetNumberOfEntities(int device);

	public native int MIDIGetDevice(int deviceIndex0);

	public native int MIDIGetNumberOfDevices();

	// MIDI Clients

	public native int MIDIClientCreate(
			CFStringRef name,
			MIDINotifyProc notifyProc,
			Pointer notifyRefCon,
			IntByReference outClient);

	public native int MIDIClientDispose(int client);

}
