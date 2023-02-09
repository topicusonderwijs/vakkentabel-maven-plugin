package nl.topicus.vakkentabel.example.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SchoolExamenTest
{
	@Test
	void isSchoolExamenVak()
	{
		assertFalse(SchoolExamen.isSchoolExamenVak(100, 666));
		assertTrue(SchoolExamen.isSchoolExamenVak(100, 1337));
		assertTrue(SchoolExamen.isSchoolExamenVak(100, 2003));

		assertFalse(SchoolExamen.isSchoolExamenVak(1001, 666));
		assertFalse(SchoolExamen.isSchoolExamenVak(1001, 1337));
		assertFalse(SchoolExamen.isSchoolExamenVak(1001, 2003));
	}
}
