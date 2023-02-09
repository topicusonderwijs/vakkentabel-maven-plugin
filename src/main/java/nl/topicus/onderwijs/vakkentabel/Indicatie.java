package nl.topicus.onderwijs.vakkentabel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Door DUO gehanteerde waardelijst voor verschillende indicaties
 *
 * @author steenbeeke
 */
public enum Indicatie
{
	VERPLICHT,
	OPTIONEEL,
	LEEG;

	private final String tabelIndicatie;

	private Indicatie()
	{
		this.tabelIndicatie = name().substring(0, 1);
	}

	@NotNull
	public String getTabelIndicatie()
	{
		return tabelIndicatie;
	}

	@Nullable
	public static Indicatie parse(@Nullable String indicatie)
	{
		for (Indicatie i : values())
		{
			if (i.getTabelIndicatie().equals(indicatie))
			{
				return i;
			}
		}

		return null;
	}
}
