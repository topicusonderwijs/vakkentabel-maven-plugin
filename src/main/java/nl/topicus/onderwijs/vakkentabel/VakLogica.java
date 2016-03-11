package nl.topicus.onderwijs.vakkentabel;

import javax.annotation.Nonnull;

public class VakLogica
{
	public static boolean heeftVakSchoolExamen(@Nonnull VakRegel vakRegel)
	{
		return vakRegel.getBeoordelingSE() == Indicatie.OPTIONEEL
			|| vakRegel.getBeoordelingSE() == Indicatie.VERPLICHT
			|| vakRegel.getCijferSE() == Indicatie.OPTIONEEL
			|| vakRegel.getCijferSE() == Indicatie.VERPLICHT;
	}

	public static boolean heeftVakCentraalExamen(@Nonnull VakRegel vakRegel)
	{
		return vakRegel.getCijferCE() == Indicatie.OPTIONEEL
			|| vakRegel.getCijferCE() == Indicatie.VERPLICHT;
	}

	public static boolean isOVGVak(@Nonnull VakRegel vakRegel)
	{
		return vakRegel.getBeoordelingSE() == Indicatie.OPTIONEEL
			|| vakRegel.getBeoordelingSE() == Indicatie.VERPLICHT;
	}
}
