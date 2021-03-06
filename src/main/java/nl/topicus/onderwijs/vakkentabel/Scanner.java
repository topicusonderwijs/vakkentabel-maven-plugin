package nl.topicus.onderwijs.vakkentabel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Parser voor CSV-bestanden in het door DUO aangeleverde formaat
 *
 * @author steenbeeke
 */
public class Scanner
{
	private static DateTimeFormatter ISO_8601_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

	private Table<Integer, Integer, VakRegel> gelezenRegels;

	final String currentDateIso8601 = ZonedDateTime.now().format(ISO_8601_FORMAT);

	public Scanner()
	{

		gelezenRegels = HashBasedTable.create();
	}

	@SuppressFBWarnings(value = "DM_DEFAULT_ENCODING",
			justification = "Er is geen manier om een encoding op te geven bij deze classes")
	public Scanner scan(File file) throws IOException
	{
		try (BufferedReader reader = new BufferedReader(new FileReader(file)))
		{

			String next = null;

			while ((next = reader.readLine()) != null)
			{
				VakRegel regel = VakRegel.parse(next);

				if (regel != null)
				{
					addRegel(regel.getRegistratieCode(), regel.getVakcode(), regel);
				}
			}
		}

		return this;
	}

	private void addRegel(int opleiding, int vakcode, @Nonnull VakRegel regel)
	{
		gelezenRegels.put(opleiding, vakcode, regel);

	}

	@CheckForNull
	public VakRegel getRegel(int opleiding, int vakcode)
	{
		return gelezenRegels.get(opleiding, vakcode);
	}

	private void generateAdvancedCheckerMethode(PrintStream printStream,
			Predicate<VakRegel> vakRegelPredicate, String methodeNaam)
	{
		printStream.println("\tpublic static boolean " + methodeNaam
			+ "(int opleidingIlt, int vakIlt)\n");
		printStream.println("\t{");
		printStream.println("\t\tswitch(opleidingIlt)");
		printStream.println("\t\t{");

		for (Integer opleiding : gelezenRegels.rowKeySet())
		{
			printStream.printf("\t\t\tcase %d: ", opleiding);
			printStream.println();

			printStream.printf("\t\t\t\treturn %sVoorIlt%d(vakIlt);", methodeNaam, opleiding);
			printStream.println();
		}

		printStream.println("\t\t}");
		printStream.println();
		printStream.println("\t\treturn false;");
		printStream.println("\t}");
		printStream.println();

		for (Entry<Integer, Map<Integer, VakRegel>> entry : gelezenRegels.rowMap().entrySet())
		{
			int opleiding = entry.getKey();

			printStream.println("\t// Omzeil Java limiet van 65K bytes code");
			printStream.printf("\tprivate static boolean %sVoorIlt%d(int vakIlt)", methodeNaam,
				opleiding);
			printStream.println();
			printStream.println("\t{");
			//
			printStream.print("\t\treturn Sets.newHashSet(");
			int i = 0;
			for (Entry<Integer, VakRegel> vakEntry : entry.getValue().entrySet())
			{
				Integer vakcode = vakEntry.getKey();
				VakRegel vakRegel = vakEntry.getValue();

				if (vakRegelPredicate.test(vakRegel))
				{
					if (i++ > 0)
						printStream.print(", ");
					printStream.printf("%d", vakcode);
				}
			}

			printStream.println(").contains(vakIlt);");
			printStream.println("\t}");
			printStream.println();
			//
		}

	}

	private void generateBeroepsgerichteVakkenRule(PrintStream ps)
	{

		ps.println("\tpublic static BronVakRule getBeroepsgerichteVakken(int iltCode)\n");
		ps.println("\t{");
		ps.println("\t\tswitch(iltCode)");
		ps.println("\t\t{");

		for (Integer opleiding : gelezenRegels.rowKeySet())
		{
			ps.printf("\t\t\tcase %d: ", opleiding);
			ps.println();

			ps.print("\t\t\t\treturn getBeroepsgerichteVakken");
			ps.print(opleiding);
			ps.println("();");
		}

		ps.println("\t\t}");
		ps.println();
		ps.println("\t\treturn reject();");
		ps.println("\t}");
		ps.println();

		for (Entry<Integer, Map<Integer, VakRegel>> entry : gelezenRegels.rowMap().entrySet())
		{
			int opleiding = entry.getKey();

			ps.print("\tprivate static BronVakRule getBeroepsgerichteVakken");
			ps.print(opleiding);
			ps.print("()\n");
			ps.println("\t{");

			ps.print("\t\treturn any(");

			int i = 0;

			for (Entry<Integer, VakRegel> vakEntry : entry.getValue().entrySet())
			{
				Integer vakcode = vakEntry.getKey();
				VakRegel vakRegel = vakEntry.getValue();

				if (vakRegel.isBeroepsgerichtVak())
				{
					if (i++ > 0)
						ps.print(", ");

					ps.printf("%d", vakcode);
				}
			}

			ps.println(");\n");
			ps.println("\t}");
			ps.println();
		}
	}

	private void generateBeroepsgerichteVakken(PrintStream printStream, String methodeNaam)
	{
		generateAdvancedCheckerMethode(printStream, VakRegel::isBeroepsgerichtVak, methodeNaam);
	}

	public void generateCentraalExamen(File packageDir, String packageName) throws IOException
	{
		createFile(
			packageDir,
			packageName,
			"CentraalExamen",
			ps -> {
				generateAdvancedCheckerMethode(ps, v -> v.getCijferCE() == Indicatie.VERPLICHT,
					"isCentraalExamenVerplichtVak");
				generateAdvancedCheckerMethode(ps, v -> v.getCijferCE() == Indicatie.OPTIONEEL,
					"isCentraalExamenOptioneelVak");
			});
	}

	public void generateSchoolExamen(File packageDir, String packageName) throws IOException
	{
		createFile(
			packageDir,
			packageName,
			"SchoolExamen",
			ps -> generateAdvancedCheckerMethode(ps, VakLogica::heeftVakSchoolExamen,
				"isSchoolExamenVak"));
	}

	public void generateBeroepsgerichtVak(File packageDir, String packageName, String matcherClass,
			String matcherFactory) throws IOException
	{
		createFile(packageDir, packageName, "BeroepsgerichtVak", ps -> {
			generateBeroepsgerichteVakken(ps, "isBeroepsgerichtVak");
			generateBeroepsgerichteVakkenRule(ps);

		}, Lists.newArrayList(matcherClass, String.format("static %s.*", matcherFactory)).stream()
			.filter(Objects::nonNull).sorted().collect(Collectors.toList()));
	}

	public void generateOVGVakken(File packageDir, String packageName) throws IOException
	{
		createFile(packageDir, packageName, "OVG",
			ps -> generateAdvancedCheckerMethode(ps, VakLogica::isOVGVak, "isOVGVak"));

	}

	public void createFile(File packageDir, String packageName, String className,
			Consumer<PrintStream> methodInvocation) throws IOException
	{
		createFile(packageDir, packageName, className, methodInvocation, ImmutableList.of());
	}

	public void createFile(File packageDir, String packageName, String className,
			Consumer<PrintStream> methodInvocation, Iterable<String> extraImports)
			throws IOException
	{
		try (FileOutputStream fos =
			new FileOutputStream(new File(packageDir, className.concat(".java")));
				PrintStream ps = new PrintStream(fos, true, "UTF-8"))
		{
			ps.printf("package %s;", packageName);
			ps.println();
			ps.println();
			ps.println("import javax.annotation.Generated;");
			ps.println();
			ps.println("import com.google.common.collect.Sets;");
			for (String i : extraImports)
			{
				ps.printf("import %s;", i);
				ps.println();
			}
			ps.println();
			ps.println("/**");
			ps.println(" * Gegeneerd door {@code nl.topicus.onderwijs.vakkentabel.VakkentabelMojo}");
			ps.println(" * @see <a");
			ps.println(" *      href=\"https://github.com/topicusonderwijs/vakkentabel-maven-plugin/wiki/Vakkentabel-DUO\">Vakkentabel");
			ps.println(" *      DUO</a>");
			ps.println(" */");
			ps.printf(
				"@Generated(value={\"nl.topicus.onderwijs.vakkentabel.VakkentabelMojo\"}, date=\"%s\")",
				currentDateIso8601);
			ps.println();
			ps.printf("public class %s {", className);
			ps.println();

			methodInvocation.accept(ps);

			ps.println("}");
			ps.flush();
		}

	}

	public int getAantalRegels()
	{
		return gelezenRegels.size();
	}
}
