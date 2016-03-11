package nl.topicus.onderwijs.vakkentabel;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

	@Nonnull
	public String getTabelIndicatie()
	{
		return tabelIndicatie;
	}

	@CheckForNull
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
