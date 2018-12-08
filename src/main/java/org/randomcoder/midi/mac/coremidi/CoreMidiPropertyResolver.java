package org.randomcoder.midi.mac.coremidi;

import org.randomcoder.midi.mac.corefoundation.CFStringRef;

public interface CoreMidiPropertyResolver {
  public CFStringRef resolve(CoreMidiProperty prop);
}