package nl.topicus.vakkentabel.example.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BeroepsgerichtVakTest
{

	@Test
	void isBeroepsgerichtVak()
	{
		assertTrue(BeroepsgerichtVak.isBeroepsgerichtVak(100, 1337));
		assertFalse(BeroepsgerichtVak.isBeroepsgerichtVak(1001, 1337));
		assertFalse(BeroepsgerichtVak.isBeroepsgerichtVak(100, 2003));
	}
}
