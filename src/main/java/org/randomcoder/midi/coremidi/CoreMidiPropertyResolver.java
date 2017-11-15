package org.randomcoder.midi.coremidi;

import org.randomcoder.midi.corefoundation.CFStringRef;

public interface CoreMidiPropertyResolver {
    public CFStringRef resolve(CoreMidiProperty prop);
}