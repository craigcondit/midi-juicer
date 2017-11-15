package org.randomcoder.midi.coremidi;

import org.randomcoder.midi.corefoundation.CFStringRef;

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public interface CoreMidiPeer extends Library {

    // MIDI Ports

    public int MIDIInputPortCreate(
	    int client,
	    CFStringRef portName,
	    MIDIReadProc readProc,
	    Pointer refCon,
	    IntByReference outPort);

    public int MIDIOutputPortCreate(int client, CFStringRef portName, IntByReference outPort);

    public int MIDIPortConnectSource(int port, int source, Pointer connRefCon);

    public int MIDIPortDisconnectSource(int port, int source);

    public int MIDIPortDispose(int port);

    // MIDI Packet Lists

    public Pointer MIDIPacketListAdd(
	    Pointer pktlist,
	    int listSize,
	    Pointer curPacket,
	    long time,
	    int nData,
	    Pointer data);

    public Pointer MIDIPacketListInit(Pointer pktlist);

    // MIDI Objects and Properties

    public int MIDIObjectFindByUniqueID(
	    int inUniqueID, IntByReference outObject, IntByReference outObjectType);

    public int MIDIObjectGetIntegerProperty(int obj, CFStringRef propertyID, Pointer outValue);

    public int MIDIObjectGetStringProperty(int obj, CFStringRef propertyID, Pointer str);

    public int MIDIObjectRemoveProperty(int obj, CFStringRef propertyID);

    public int MIDIObjectSetIntegerProperty(int obj, CFStringRef propertyID, int value);

    public int MIDIObjectSetStringProperty(int obj, CFStringRef propertyID, CFStringRef str);

    // MIDI I/O

    public int MIDIFlushOutput(int dest);

    public int MIDIReceived(int src, Pointer pktlist);

    public int MIDIRestart();

    public int MIDISend(int port, int dest, Pointer pktlist);

    public int MIDISendSysex(Pointer request);

    // MIDI External Devices

    public int MIDIGetNumberOfExternalDevices();

    public int MIDIGetExternalDevice(int deviceIndex0);

    // MIDI Entities

    public int MIDIEntityGetDestination(int entity, int destIndex0);

    public int MIDIEntityGetDevice(int inEntity, IntByReference outDevice);

    public int MIDIEntityGetNumberOfDestinations(int entity);

    public int MIDIEntityGetNumberOfSources(int entity);

    public int MIDIEntityGetSource(int entity, int sourceIndex0);

    // MIDI Endpoints

    public int MIDIDestinationCreate(
	    int client,
	    CFStringRef name,
	    MIDIReadProc readProc,
	    Pointer refCon,
	    IntByReference outDest);

    public int MIDIEndpointDispose(int endpt);

    public int MIDIEndpointGetEntity(int inEndpoint, IntByReference outEntity);

    public int MIDIGetDestination(int destIndex0);

    public int MIDIGetNumberOfDestinations();

    public int MIDIGetNumberOfSources();

    public int MIDIGetSource(int sourceIndex0);

    public int MIDISourceCreate(int client, CFStringRef name, IntByReference outSrc);

    // MIDI Devices

    public int MIDIDeviceGetEntity(int device, int entityIndex0);

    public int MIDIDeviceGetNumberOfEntities(int device);

    public int MIDIGetDevice(int deviceIndex0);

    public int MIDIGetNumberOfDevices();

    // MIDI Clients

    public int MIDIClientCreate(
	    CFStringRef name,
	    MIDINotifyProc notifyProc,
	    Pointer notifyRefCon,
	    IntByReference outClient);

    public int MIDIClientDispose(int client);

}
