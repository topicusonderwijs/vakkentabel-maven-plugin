package nl.topicus.onderwijs.vakkentabel;

import org.jetbrains.annotations.NotNull;

public class VakLogica
{
	public static boolean heeftVakSchoolExamen(@NotNull VakRegel vakRegel)
	{
		return vakRegel.getBeoordelingSE() == Indicatie.OPTIONEEL
			|| vakRegel.getBeoordelingSE() == Indicatie.VERPLICHT
			|| vakRegel.getCijferSE() == Indicatie.OPTIONEEL
			|| vakRegel.getCijferSE() == Indicatie.VERPLICHT;
	}

	public static boolean heeftVakCentraalExamen(@NotNull VakRegel vakRegel)
	{
		return vakRegel.getCijferCE() == Indicatie.OPTIONEEL
			|| vakRegel.getCijferCE() == Indicatie.VERPLICHT;
	}

	public static boolean isOVGVak(@NotNull VakRegel vakRegel)
	{
		return vakRegel.getBeoordelingSE() == Indicatie.OPTIONEEL
			|| vakRegel.getBeoordelingSE() == Indicatie.VERPLICHT;
	}
}
