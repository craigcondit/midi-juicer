package org.randomcoder.midi.mac.corefoundation;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class CFRunLoopSourceContext extends Structure {
  public int version = 0;
  public Pointer info = null; // must be set
  public Pointer retain = null; // can be null
  public Pointer release = null; // can be null
  public Pointer copyDescription = null; // can be null
  public Pointer equal = null; // can be null
  public Pointer hash = null; // can be null
  public Pointer schedule = null; // can be null
  public Pointer cancel = null; // can be null
  public CFRunLoopSourcePerform perform; // must be set

  public CFRunLoopSourceContext() {
  }

  public CFRunLoopSourceContext(Pointer info, CFRunLoopSourcePerform context) {
    this.version = 0;
    this.info = info;
    this.perform = context;
  }

  @Override protected List<String> getFieldOrder() {
    return Arrays
        .asList("version", "info", "retain", "release", "copyDescription",
            "equal", "hash", "schedule", "cancel", "perform");
  }
}
