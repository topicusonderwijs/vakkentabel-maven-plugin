package nl.topicus.onderwijs.vakkentabel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Parser voor CSV-bestanden in het door DUO aangeleverde formaat
 *
 * @author steenbeeke
 */
public class Scanner
{

	private final Table<Integer, Integer, VakRegel> gelezenRegels;

	final String currentDateIso8601 =
		ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

	public Scanner()
	{
		gelezenRegels = HashBasedTable.create();
	}

	public Scanner scan(File file) throws IOException
	{
		try (BufferedReader reader =
			new BufferedReader(new FileReader(file, StandardCharsets.UTF_8)))
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

	private void addRegel(int opleiding, int vakcode, @NotNull VakRegel regel)
	{
		gelezenRegels.put(opleiding, vakcode, regel);

	}

	@Nullable
	public VakRegel getRegel(int opleiding, int vakcode)
	{
		return gelezenRegels.get(opleiding, vakcode);
	}

	private void generateAdvancedCheckerMethode(@NotNull PrintStream printStream,
			@NotNull Predicate<VakRegel> vakRegelPredicate, @NotNull String methodeNaam)
	{
		printStream
			.println("\tpublic static boolean " + methodeNaam + "(int opleidingIlt, int vakIlt)\n");
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
			printStream.print("\t\treturn Set.of(");
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

	private void generateBeroepsgerichteVakkenRule(@NotNull PrintStream ps, @NotNull String matcherClass)
	{

		ps.printf("\tpublic static %s getBeroepsgerichteVakken(int iltCode)%n", matcherClass);
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

			ps.printf("\tprivate static %s getBeroepsgerichteVakken", matcherClass);
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

	private void generateBeroepsgerichteVakken(@NotNull PrintStream printStream,
			@NotNull String methodeNaam)
	{
		generateAdvancedCheckerMethode(printStream, VakRegel::isBeroepsgerichtVak, methodeNaam);
	}

	public void generateCentraalExamen(@NotNull File packageDir, @NotNull String packageName)
			throws IOException
	{
		createFile(packageDir, packageName, "CentraalExamen", ps -> {
			generateAdvancedCheckerMethode(ps, v -> v.getCijferCE() == Indicatie.VERPLICHT,
				"isCentraalExamenVerplichtVak");
			generateAdvancedCheckerMethode(ps, v -> v.getCijferCE() == Indicatie.OPTIONEEL,
				"isCentraalExamenOptioneelVak");
		});
	}

	public void generateSchoolExamen(@NotNull File packageDir, @NotNull String packageName)
			throws IOException
	{
		createFile(packageDir, packageName, "SchoolExamen", ps -> generateAdvancedCheckerMethode(ps,
			VakLogica::heeftVakSchoolExamen, "isSchoolExamenVak"));
	}

	public void generateBeroepsgerichtVak(@NotNull File packageDir, @NotNull String packageName,
			@NotNull String matcherClass, @NotNull String matcherFactory) throws IOException
	{
		createFile(packageDir, packageName, "BeroepsgerichtVak", ps -> {
			generateBeroepsgerichteVakken(ps, "isBeroepsgerichtVak");
			generateBeroepsgerichteVakkenRule(ps, matcherClass);

		}, Stream.of(matcherClass, String.format("static %s.*", matcherFactory))
			.filter(Objects::nonNull)
			.sorted()
			.collect(Collectors.toList()));
	}

	public void generateOVGVakken(@NotNull File packageDir, @NotNull String packageName)
			throws IOException
	{
		createFile(packageDir, packageName, "OVG",
			ps -> generateAdvancedCheckerMethode(ps, VakLogica::isOVGVak, "isOVGVak"));

	}

	public void createFile(@NotNull File packageDir, @NotNull String packageName,
			@NotNull String className, @NotNull Consumer<PrintStream> methodInvocation)
			throws IOException
	{
		createFile(packageDir, packageName, className, methodInvocation, List.of());
	}

	public void createFile(@NotNull File packageDir, @NotNull String packageName,
			@NotNull String className, @NotNull Consumer<PrintStream> methodInvocation,
			@NotNull Iterable<String> extraImports) throws IOException
	{
		try (
			FileOutputStream fos =
				new FileOutputStream(new File(packageDir, className.concat(".java")));
			PrintStream ps = new PrintStream(fos, true, "UTF-8"))
		{
			ps.printf("package %s;", packageName);
			ps.println();
			ps.println();
			ps.println("import jakarta.annotation.Generated;");
			ps.println("import java.util.Set;");
			ps.println();
			for (String i : extraImports)
			{
				ps.printf("import %s;", i);
				ps.println();
			}
			ps.println();
			ps.println("/**");
			ps.println(
				" * Gegeneerd door {@code nl.topicus.onderwijs.vakkentabel.VakkentabelMojo}");
			ps.println(" * @see <a");
			ps.println(
				" *      href=\"https://github.com/topicusonderwijs/vakkentabel-maven-plugin/wiki/Vakkentabel-DUO\">Vakkentabel");
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
