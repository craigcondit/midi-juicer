package org.randomcoder.midi.corefoundation;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.jna.Memory;

public class CFRangeTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void emptyConstructorCreatesEmptyObject() {
		CFRange obj = new CFRange();
		assertEquals(0L, obj.loc);
		assertEquals(0L, obj.len);
	}

	@Test
	public void pointerConstructorInitializesObjectFromMemory() {
		Memory m = new Memory(20L);
		m.setLong(4L, 123L);
		m.setLong(12L, 456L);
		CFRange obj = new CFRange(m, 4);
		assertEquals(123L, obj.loc);
		assertEquals(456L, obj.len);
	}

	@Test
	public void valueConstructorCreatesPopulatedObject() {
		CFRange obj = new CFRange(111L, 222L);
		assertEquals(111L, obj.loc);
		assertEquals(222L, obj.len);
	}

	@Test
	public void byReferenceEmptyConstructorCreatesEmptyObject() {
		CFRange obj = new CFRange.ByReference();
		assertEquals(0L, obj.loc);
		assertEquals(0L, obj.len);
	}

	@Test
	public void byReferenceCopyConstructorCreatesEquivalentObject() {
		CFRange src = new CFRange(1L, 2L);
		src.write();
		CFRange obj = new CFRange.ByReference(src);
		assertEquals(1L, obj.loc);
		assertEquals(2L, obj.len);
	}

	@Test
	public void byValueEmptyConstructorCreatesEmptyObject() {
		CFRange obj = new CFRange.ByValue();
		assertEquals(0L, obj.loc);
		assertEquals(0L, obj.len);
	}

	@Test
	public void byValueCopyConstructorCreatesEquivalentObject() {
		CFRange src = new CFRange(1L, 2L);
		src.write();
		CFRange obj = new CFRange.ByValue(src);
		assertEquals(1L, obj.loc);
		assertEquals(2L, obj.len);
	}

}
