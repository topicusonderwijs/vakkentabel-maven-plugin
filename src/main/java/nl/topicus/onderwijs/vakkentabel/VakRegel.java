package nl.topicus.onderwijs.vakkentabel;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class VakRegel
{
	private static final String BOOLEAN_JA = "J";

	private final int vakcode;

	private final String naamVakKort;

	private final String typeRegistratieCode;

	private final int registratieCode;

	private final int studieLastUren;

	private final boolean beroepsgerichtVak;

	private final Indicatie beoordelingSE;

	private final Indicatie cijferSE;

	private final Indicatie cijferCE;

	private final Indicatie eindcijfer;

	private final Indicatie cijferCijferlijst;

	private final Indicatie combinatieCijfer;

	private final boolean combinatieCijferToegestaan;

	private VakRegel(int vakcode, String naamVakKort, String typeRegistratieCode,
			int registratieCode, int studieLastUren, boolean beroepsgerichtVak,
			@Nonnull Indicatie beoordelingSE, @Nonnull Indicatie cijferSE,
			@Nonnull Indicatie cijferCE, @Nonnull Indicatie eindcijfer,
			@Nonnull Indicatie cijferCijferlijst, @Nonnull Indicatie combinatieCijfer,
			boolean combinatieCijferToegestaan)
	{
		super();
		this.vakcode = vakcode;
		this.naamVakKort = naamVakKort;
		this.typeRegistratieCode = typeRegistratieCode;
		this.registratieCode = registratieCode;
		this.studieLastUren = studieLastUren;
		this.beroepsgerichtVak = beroepsgerichtVak;
		this.beoordelingSE = beoordelingSE;
		this.cijferSE = cijferSE;
		this.cijferCE = cijferCE;
		this.eindcijfer = eindcijfer;
		this.cijferCijferlijst = cijferCijferlijst;
		this.combinatieCijfer = combinatieCijfer;
		this.combinatieCijferToegestaan = combinatieCijferToegestaan;
	}

	public int getVakcode()
	{
		return vakcode;
	}

	public String getNaamVakKort()
	{
		return naamVakKort;
	}

	public String getTypeRegistratieCode()
	{
		return typeRegistratieCode;
	}

	public int getRegistratieCode()
	{
		return registratieCode;
	}

	public int getStudieLastUren()
	{
		return studieLastUren;
	}

	public boolean isBeroepsgerichtVak()
	{
		return beroepsgerichtVak;
	}

	@Nonnull
	public Indicatie getBeoordelingSE()
	{
		return beoordelingSE;
	}

	@Nonnull
	public Indicatie getCijferSE()
	{
		return cijferSE;
	}

	@Nonnull
	public Indicatie getCijferCE()
	{
		return cijferCE;
	}

	@Nonnull
	public Indicatie getEindcijfer()
	{
		return eindcijfer;
	}

	@Nonnull
	public Indicatie getCijferCijferlijst()
	{
		return cijferCijferlijst;
	}

	@Nonnull
	public Indicatie getCombinatieCijfer()
	{
		return combinatieCijfer;
	}

	public boolean isCombinatieCijferToegestaan()
	{
		return combinatieCijferToegestaan;
	}

	@CheckForNull
	public static VakRegel parse(String line)
	{
		String[] fields = line.split(";");

		if (fields.length < 13)
		{
			return null;
		}

		try
		{
			final int vakcode = Integer.parseInt(fields[0]);

			final String naamVakKort = fields[1];

			final String typeRegistratieCode = fields[2];

			final int registratieCode = Integer.parseInt(fields[3]);

			final int studieLastUren = Integer.parseInt(fields[4]);

			final boolean beroepsgerichtVak = Integer.parseInt(fields[5]) != 0;

			final Indicatie beoordelingSE = Indicatie.parse(fields[6]);

			final Indicatie cijferSE = Indicatie.parse(fields[7]);

			final Indicatie cijferCE = Indicatie.parse(fields[8]);

			final Indicatie eindcijfer = Indicatie.parse(fields[9]);

			final Indicatie cijferCijferlijst = Indicatie.parse(fields[10]);

			final Indicatie combinatieCijfer = Indicatie.parse(fields[11]);

			final boolean combinatieCijferToegestaan = BOOLEAN_JA.equals(fields[12]);

			if (beoordelingSE == null || cijferSE == null || cijferCE == null || eindcijfer == null
				|| cijferCijferlijst == null || combinatieCijfer == null)
			{
				// Onjuist geformatteerde regel
				return null;
			}

			return new VakRegel(vakcode, naamVakKort, typeRegistratieCode, registratieCode,
				studieLastUren, beroepsgerichtVak, beoordelingSE, cijferSE, cijferCE, eindcijfer,
				cijferCijferlijst, combinatieCijfer, combinatieCijferToegestaan);
		}
		catch (NumberFormatException nfe)
		{
			return null;
		}

	}
}
