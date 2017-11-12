package org.randomcoder.midi.coremidi;

import org.randomcoder.midi.corefoundation.CFStringRef;

public enum CoreMidiProperty {

	kMIDIPropertyName,
	kMIDIPropertyManufacturer,
	kMIDIPropertyModel,
	kMIDIPropertyUniqueID,
	kMIDIPropertyDeviceID,
	kMIDIPropertyReceiveChannels,
	kMIDIPropertyTransmitChannels,
	kMIDIPropertyMaxSysExSpeed,
	kMIDIPropertyAdvanceScheduleTimeMuSec,
	kMIDIPropertyIsEmbeddedEntity,
	kMIDIPropertyIsBroadcast,
	kMIDIPropertySingleRealtimeEntity,
	kMIDIPropertyConnectionUniqueID,
	kMIDIPropertyOffline,
	kMIDIPropertyPrivate,
	kMIDIPropertyDriverOwner,
	@Deprecated
	kMIDIPropertyFactoryPatchNameFile,
	@Deprecated
	kMIDIPropertyUserPatchNameFile,
	kMIDIPropertyNameConfiguration,
	kMIDIPropertyImage,
	kMIDIPropertyDriverVersion,
	kMIDIPropertySupportsGeneralMIDI,
	kMIDIPropertySupportsMMC,
	kMIDIPropertyCanRoute,
	kMIDIPropertyReceivesClock,
	kMIDIPropertyReceivesMTC,
	kMIDIPropertyReceivesNotes,
	kMIDIPropertyReceivesProgramChanges,
	kMIDIPropertyReceivesBankSelectMSB,
	kMIDIPropertyReceivesBankSelectLSB,
	kMIDIPropertyTransmitsClock,
	kMIDIPropertyTransmitsMTC,
	kMIDIPropertyTransmitsNotes,
	kMIDIPropertyTransmitsProgramChanges,
	kMIDIPropertyTransmitsBankSelectMSB,
	kMIDIPropertyTransmitsBankSelectLSB,
	kMIDIPropertyPanDisruptsStereo,
	kMIDIPropertyIsSampler,
	kMIDIPropertyIsDrumMachine,
	kMIDIPropertyIsMixer,
	kMIDIPropertyIsEffectUnit,
	kMIDIPropertyMaxReceiveChannels,
	kMIDIPropertyMaxTransmitChannels,
	kMIDIPropertyDriverDeviceEditorApp,
	kMIDIPropertySupportsShowControl,
	kMIDIPropertyDisplayName;

	public CFStringRef resolve(CoreMidiPropertyResolver resolver) {
		return resolver.resolve(this);
	}
}