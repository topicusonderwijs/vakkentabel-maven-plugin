package nl.topicus.onderwijs.vakkentabel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class Scanner
{
	private static DateTimeFormatter iso8601format = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

	private Multimap<Integer, Integer> vakOpleidingMapping;

	private Multimap<Integer, Integer> beroepsGerichteVakken;

	private Map<String, Multimap<Integer, Integer>> seIndicaties;

	private Map<String, Multimap<Integer, Integer>> ceIndicaties;

	private Map<String, Multimap<Integer, Integer>> eindIndicaties;

	private Map<String, Multimap<Integer, Integer>> cijferLijstIndicaties;

	final String currentDateIso8601 = LocalDateTime.now().format(iso8601format);

	public Scanner()
	{
		vakOpleidingMapping = TreeMultimap.create();
		beroepsGerichteVakken = TreeMultimap.create();
		ceIndicaties = Maps.newTreeMap();
		seIndicaties = Maps.newTreeMap();
		eindIndicaties = Maps.newTreeMap();
		cijferLijstIndicaties = Maps.newTreeMap();
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
				String[] fields = next.split(",");

				if (fields.length > 11)
				{
					String vakcode = fields[0];
					String opleidingCode = fields[3];

					try
					{
						Integer vakElementCode = Integer.parseInt(vakcode);
						Integer opleidingElementCode = Integer.parseInt(opleidingCode);

						if (vakElementCode != null && opleidingElementCode != null)
						{
							vakOpleidingMapping.put(opleidingElementCode, vakElementCode);

							String seIndicatie = fields[7];
							String ceIndicatie = fields[8];

							if (fields[5].equals("1"))
							{
								beroepsGerichteVakken.put(opleidingElementCode, vakElementCode);
								ceIndicatie = "V";
							}

							addIndicatie(seIndicaties, vakElementCode, opleidingElementCode,
								seIndicatie);
							addIndicatie(ceIndicaties, vakElementCode, opleidingElementCode,
								ceIndicatie);
							addIndicatie(eindIndicaties, vakElementCode, opleidingElementCode,
								fields[9]);
							addIndicatie(cijferLijstIndicaties, vakElementCode,
								opleidingElementCode, fields[10]);

						}

					}
					catch (NumberFormatException nfe)
					{
						// Skip record
					}
				}
			}
		}

		return this;
	}

	private void addIndicatie(Map<String, Multimap<Integer, Integer>> target,
			Integer vakElementCode, Integer opleidingElementCode, String indicatieType)
	{
		if (!target.containsKey(indicatieType))
		{
			target.put(indicatieType, TreeMultimap.<Integer, Integer> create());
		}

		target.get(indicatieType).put(opleidingElementCode, vakElementCode);
	}

	private void generateCheckerMethode(PrintStream printStream,
			Map<Integer, Collection<Integer>> map, String methodeNaam)
	{
		printStream.println("\tpublic static boolean " + methodeNaam
			+ "(int opleidingIlt, int vakIlt)\n");
		printStream.println("\t{");
		printStream.println("\t\tswitch(opleidingIlt)");
		printStream.println("\t\t{");

		for (Entry<Integer, Collection<Integer>> e : map.entrySet())
		{
			printStream.printf("\t\t\tcase %d: ", e.getKey());
			printStream.println();

			int i = 0;

			printStream.print("\t\t\t\treturn Sets.newHashSet(");

			for (Integer ilt : e.getValue())
			{
				if (i++ > 0)
					printStream.print(", ");
				printStream.printf("%d", ilt);
			}

			printStream.println(").contains(vakIlt);");
		}
		printStream.println("\t\t}");
		printStream.println();
		printStream.println("\t\treturn false;");
		printStream.println("\t}");
	}

	private void generateAdvancedCheckerMethode(PrintStream printStream,
			Map<Integer, Collection<Integer>> map, String methodeNaam)
	{
		printStream.println("\tpublic static boolean " + methodeNaam
			+ "(int opleidingIlt, int vakIlt)\n");
		printStream.println("\t{");
		printStream.println("\t\tswitch(opleidingIlt)");
		printStream.println("\t\t{");

		for (Entry<Integer, Collection<Integer>> e : map.entrySet())
		{
			printStream.printf("\t\t\tcase %d: ", e.getKey());
			printStream.println();

			printStream.printf("\t\t\t\treturn %sVoorIlt%d(vakIlt);", methodeNaam, e.getKey());
			printStream.println();
		}
		printStream.println("\t\t}");
		printStream.println();
		printStream.println("\t\treturn false;");
		printStream.println("\t}");
		printStream.println();

		for (Entry<Integer, Collection<Integer>> e : map.entrySet())
		{
			printStream.println("\t// Omzeil Java limiet van 65K bytes code");
			printStream.printf("\tprivate static boolean %sVoorIlt%d(int vakIlt)", methodeNaam,
				e.getKey());
			printStream.println();
			printStream.println("\t{");
			//
			printStream.print("\t\treturn Sets.newHashSet(");
			int i = 0;
			for (Integer ilt : e.getValue())
			{
				if (i++ > 0)
					printStream.print(", ");
				printStream.printf("%d", ilt);
			}

			printStream.println(").contains(vakIlt);");
			printStream.println("\t}");
			printStream.println();
			//
		}

	}

	private void generateBeroepsgerichteVakkenRule(PrintStream ps)
	{
		Map<Integer, Collection<Integer>> map = beroepsGerichteVakken.asMap();

		ps.println("\tpublic static BronVakRule getBeroepsgerichteVakken(int iltCode)\n");
		ps.println("\t{");
		ps.println("\t\tswitch(iltCode)");
		ps.println("\t\t{");

		for (Entry<Integer, Collection<Integer>> e : map.entrySet())
		{
			ps.printf("\t\t\tcase %d: ", e.getKey());
			ps.println();

			ps.print("\t\t\t\treturn getBeroepsgerichteVakken");
			ps.print(e.getKey());
			ps.println("();");
		}

		ps.println("\t\t}");
		ps.println();
		ps.println("\t\treturn reject();");
		ps.println("\t}");
		ps.println();

		for (Entry<Integer, Collection<Integer>> e : map.entrySet())
		{

			ps.print("\tprivate static BronVakRule getBeroepsgerichteVakken");
			ps.print(e.getKey());
			ps.print("()\n");
			ps.println("\t{");

			ps.print("\t\treturn any(");

			int i = 0;
			for (Integer ilt : e.getValue())
			{
				if (i++ > 0)
					ps.print(", ");
				ps.printf("%d", ilt);
			}

			ps.println(");\n");
			ps.println("\t}");
			ps.println();
		}
	}

	private void generateBeroepsgerichteVakken(PrintStream printStream, String methodeNaam)
	{
		Map<Integer, Collection<Integer>> map = beroepsGerichteVakken.asMap();

		generateAdvancedCheckerMethode(printStream, map, methodeNaam);
	}

	private void generateSeVakkenVanType(PrintStream printStream, String methodeNaam,
			String indicatieType)
	{
		Map<Integer, Collection<Integer>> map = seIndicaties.get(indicatieType).asMap();

		generateAdvancedCheckerMethode(printStream, map, methodeNaam);
	}

	private void generateCeVakkenVanType(PrintStream printStream, String methodeNaam,
			String indicatieType)
	{
		Map<Integer, Collection<Integer>> map = ceIndicaties.get(indicatieType).asMap();

		generateAdvancedCheckerMethode(printStream, map, methodeNaam);
	}

	@SuppressWarnings("unused")
	private void generateEindVakkenVanType(PrintStream printStream, String methodeNaam,
			String indicatieType)
	{
		Map<Integer, Collection<Integer>> map = eindIndicaties.get(indicatieType).asMap();

		generateCheckerMethode(printStream, map, methodeNaam);
	}

	private void generateCijferLijstVakkenVanType(PrintStream printStream, String methodeNaam,
			String indicatieType)
	{
		Map<Integer, Collection<Integer>> map = cijferLijstIndicaties.get(indicatieType).asMap();

		generateAdvancedCheckerMethode(printStream, map, methodeNaam);
	}

	public void generateCentraalExamen(File packageDir, String packageName) throws IOException
	{
		createFile(packageDir, packageName, "CentraalExamen",
			ps -> generateCeVakkenVanType(ps, "isCentraalExamenVak", "V"));
	}

	public void generateSchoolExamen(File packageDir, String packageName) throws IOException
	{
		createFile(packageDir, packageName, "SchoolExamen",
			ps -> generateSeVakkenVanType(ps, "isSchoolExamenVak", "V"));
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
			ps -> generateCijferLijstVakkenVanType(ps, "isOVGVak", "L"));
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
}
