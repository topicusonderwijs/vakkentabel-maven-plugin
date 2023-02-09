package nl.topicus.vakkentabel.example.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OVGTest
{

	@Test
	void isOVGVak()
	{
		assertFalse(OVG.isOVGVak(100, 666));
		assertTrue(OVG.isOVGVak(100, 1337));
		assertTrue(OVG.isOVGVak(100, 2003));

		assertFalse(OVG.isOVGVak(1001, 666));
		assertFalse(OVG.isOVGVak(1001, 1337));
		assertFalse(OVG.isOVGVak(1001, 2003));
	}
}
