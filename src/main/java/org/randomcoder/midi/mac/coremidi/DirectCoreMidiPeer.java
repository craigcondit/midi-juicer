package org.randomcoder.midi.mac.coremidi;

import org.randomcoder.midi.mac.corefoundation.CFStringRef;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class DirectCoreMidiPeer implements CoreMidiPeer {

	static {
		Native.register(CoreMidiServiceFactory.LIBRARY_NAME);
	}

	// MIDI Ports

	@Override
	public native int MIDIInputPortCreate(
			int client,
			CFStringRef portName,
			MIDIReadProc readProc,
			Pointer refCon,
			IntByReference outPort);

	@Override
	public native int MIDIOutputPortCreate(int client, CFStringRef portName, IntByReference outPort);

	@Override
	public native int MIDIPortConnectSource(int port, int source, Pointer connRefCon);

	@Override
	public native int MIDIPortDisconnectSource(int port, int source);

	@Override
	public native int MIDIPortDispose(int port);

	// MIDI Packet Lists

	@Override
	public native Pointer MIDIPacketListAdd(
			Pointer pktlist,
			int listSize,
			Pointer curPacket,
			long time,
			int nData,
			Pointer data);

	@Override
	public native Pointer MIDIPacketListInit(Pointer pktlist);

	// MIDI Objects and Properties

	@Override
	public native int MIDIObjectFindByUniqueID(
			int inUniqueID, IntByReference outObject, IntByReference outObjectType);

	@Override
	public native int MIDIObjectGetIntegerProperty(int obj, CFStringRef propertyID, Pointer outValue);

	@Override
	public native int MIDIObjectGetStringProperty(int obj, CFStringRef propertyID, Pointer str);

	@Override
	public native int MIDIObjectRemoveProperty(int obj, CFStringRef propertyID);

	@Override
	public native int MIDIObjectSetIntegerProperty(int obj, CFStringRef propertyID, int value);

	@Override
	public native int MIDIObjectSetStringProperty(int obj, CFStringRef propertyID, CFStringRef str);

	// MIDI I/O

	@Override
	public native int MIDIFlushOutput(int dest);

	@Override
	public native int MIDIReceived(int src, Pointer pktlist);

	@Override
	public native int MIDIRestart();

	@Override
	public native int MIDISend(int port, int dest, Pointer pktlist);

	@Override
	public native int MIDISendSysex(Pointer request);

	// MIDI External Devices

	@Override
	public native int MIDIGetNumberOfExternalDevices();

	@Override
	public native int MIDIGetExternalDevice(int deviceIndex0);

	// MIDI Entities

	@Override
	public native int MIDIEntityGetDestination(int entity, int destIndex0);

	@Override
	public native int MIDIEntityGetDevice(int inEntity, IntByReference outDevice);

	@Override
	public native int MIDIEntityGetNumberOfDestinations(int entity);

	@Override
	public native int MIDIEntityGetNumberOfSources(int entity);

	@Override
	public native int MIDIEntityGetSource(int entity, int sourceIndex0);

	// MIDI Endpoints

	@Override
	public native int MIDIDestinationCreate(
			int client,
			CFStringRef name,
			MIDIReadProc readProc,
			Pointer refCon,
			IntByReference outDest);

	@Override
	public native int MIDIEndpointDispose(int endpt);

	@Override
	public native int MIDIEndpointGetEntity(int inEndpoint, IntByReference outEntity);

	@Override
	public native int MIDIGetDestination(int destIndex0);

	@Override
	public native int MIDIGetNumberOfDestinations();

	@Override
	public native int MIDIGetNumberOfSources();

	@Override
	public native int MIDIGetSource(int sourceIndex0);

	@Override
	public native int MIDISourceCreate(int client, CFStringRef name, IntByReference outSrc);

	// MIDI Devices

	@Override
	public native int MIDIDeviceGetEntity(int device, int entityIndex0);

	@Override
	public native int MIDIDeviceGetNumberOfEntities(int device);

	@Override
	public native int MIDIGetDevice(int deviceIndex0);

	@Override
	public native int MIDIGetNumberOfDevices();

	// MIDI Clients

	@Override
	public native int MIDIClientCreate(
			CFStringRef name,
			MIDINotifyProc notifyProc,
			Pointer notifyRefCon,
			IntByReference outClient);

	@Override
	public native int MIDIClientDispose(int client);

}
