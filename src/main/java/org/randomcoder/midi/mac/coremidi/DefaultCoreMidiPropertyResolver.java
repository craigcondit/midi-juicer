package org.randomcoder.midi.mac.coremidi;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.randomcoder.midi.mac.corefoundation.CFStringRef;

class DefaultCoreMidiPropertyResolver implements CoreMidiPropertyResolver {

	private final ConcurrentMap<CoreMidiProperty, CFStringRef> cache = new ConcurrentHashMap<>();

	@Override
	public CFStringRef resolve(CoreMidiProperty prop) {
		Objects.requireNonNull(prop, "prop cannot be null");
		return cache.computeIfAbsent(prop, k -> {
			return new CFStringRef(CoreMidiServiceFactory.getNativeLibrary()
					.getGlobalVariableAddress(k.name()).getPointer(0));
		});
	}

}