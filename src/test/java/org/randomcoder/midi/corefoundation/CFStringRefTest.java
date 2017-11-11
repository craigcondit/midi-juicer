package org.randomcoder.midi.corefoundation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;

public class CFStringRefTest {
	CoreFoundationPeer cf;

	@Before
	public void setUp() throws Exception {
		cf = mock(CoreFoundationPeer.class);
		CoreFoundationServiceFactory.setPeer(cf);
	}

	@After
	public void tearDown() throws Exception {
		CoreFoundationServiceFactory.setPeer(null);
	}

	@Test
	public void emptyConstructorReferencesNullPointer() {
		CFStringRef ref = new CFStringRef();
		assertEquals(0L, Pointer.nativeValue(ref.getValue()));
	}

	@Test
	public void pointerConstructorRetainsValue() {
		Memory m = new Memory(8);
		CFStringRef ref = new CFStringRef(m);
		assertEquals(m, ref.getPointer());
	}

	@Test
	public void createNativeCallsCFStringCreateWithCharacters() {
		char[] data = "test".toCharArray();
		CFStringRef ref = new CFStringRef();

		when(cf.CFStringCreateWithCharacters(null, data, 4)).thenReturn(ref);
		assertSame(ref, CFStringRef.createNative("test"));
	}

	@Test
	public void toStringWithZeroLengthReturnsEmptyString() {
		CFStringRef obj = new CFStringRef();
		when(cf.CFStringGetLength(obj)).thenReturn(0);
		assertEquals("", obj.toString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void toStringFailsIfStringIsHuge() {
		Memory m = new Memory(8);
		CFStringRef obj = new CFStringRef(m);
		when(cf.CFStringGetLength(obj)).thenReturn(Integer.MAX_VALUE);
		obj.toString();
	}

	@Test
	public void toStringHandlesEncodingProperly() {
		byte[] data = "test".getBytes(StandardCharsets.UTF_16LE);
		Memory m = new Memory(8);
		m.write(0L, data, 0, data.length);

		CFStringRef obj = new CFStringRef(m);
		when(cf.CFStringGetLength(obj)).thenReturn(4);

		// stub out what native code does
		doAnswer(i -> {
			assertSame("first argument must be the object under test", obj, i.getArgument(0));
			CFRange range = (CFRange) i.getArgument(1);
			assertEquals("wrong loc value", 0L, range.loc);
			assertEquals("wrong len value", 4L, range.len);
			Memory m2 = (Memory) i.getArgument(2);
			m2.write(0L, data, 0, data.length);
			return null;
		}).when(cf).CFStringGetCharacters(any(CFStringRef.class), any(CFRange.ByValue.class), any(Memory.class));

		assertEquals("test", obj.toString());
	}

}
