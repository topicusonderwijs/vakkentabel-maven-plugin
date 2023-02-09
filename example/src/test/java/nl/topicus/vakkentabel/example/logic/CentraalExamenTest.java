package nl.topicus.vakkentabel.example.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CentraalExamenTest
{

	@Test
	void isCentraalExamenVerplichtVak()
	{
		assertTrue(CentraalExamen.isCentraalExamenVerplichtVak(100, 666));
		assertFalse(CentraalExamen.isCentraalExamenVerplichtVak(100, 1337));
		assertFalse(CentraalExamen.isCentraalExamenVerplichtVak(100, 2003));
		assertFalse(CentraalExamen.isCentraalExamenVerplichtVak(1001, 666));
	}

	@Test
	void isCentraalExamenOptioneelVak()
	{
		assertFalse(CentraalExamen.isCentraalExamenOptioneelVak(100, 666));
		assertFalse(CentraalExamen.isCentraalExamenOptioneelVak(100, 1337));
		assertTrue(CentraalExamen.isCentraalExamenOptioneelVak(100, 2003));
		assertFalse(CentraalExamen.isCentraalExamenOptioneelVak(1001, 2003));
	}
}
