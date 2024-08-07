package nl.topicus.onderwijs.vakkentabel;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.jetbrains.annotations.NotNull;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Maven Goal voor het genereren van utility classes op basis van de vakkentabel van DUO
 *
 * <a href=
 * "http://www.duo.nl/zakelijk/klantenservice/softwareleveranciers/Programmas_van_eisen.asp"
 * >http://www.duo.nl/zakelijk/klantenservice/softwareleveranciers/Programmas_van_eisen. asp</a>.
 *
 * Voor uitleg rondom deze Maven plugin:
 *
 * <a href= "https://github.com/topicusonderwijs/vakkentabel-maven-plugin/wiki/Vakkentabel-DUO"
 * >Vakkentabel DUO</a>
 *
 * @author Jeroen Steenbeeke
 * @author Sven Haster
 */
@Mojo(defaultPhase = LifecyclePhase.GENERATE_SOURCES, name = "generate", threadSafe = true,
		requiresDependencyResolution = ResolutionScope.TEST)
public class VakkentabelMojo extends AbstractMojo
{
	/**
	 * De locatie van de vakkentabel (relatief tov pom-dir, of absoluut tov systeem - dat laatste
	 * wil je niet)
	 */
	@Parameter(required = true)
	public File vakkentabel;

	/**
	 * De package waarbinnen de gegenereerde classes moeten worden geplaatst (onder
	 * target/generated-sources/vakkentabel )
	 */
	@Parameter(required = true)
	public String packagePrefix;

	/**
	 * Type dat gebruikt wordt voor methoden die matchers terug geven
	 */
	@SuppressWarnings("javadoc")
	@Parameter(required = true)
	public String matcherClass;

	/**
	 * Type dat gebruikt wordt voor het aanmaken van bovenstaande matchers
	 */
	@SuppressWarnings("javadoc")
	@Parameter(required = true)
	public String matcherFactory;

	@Parameter(required = true,
			defaultValue = "${project.build.directory}/generated-sources/vakkentabel")
	public File outputDirectory;

	@Component
	public BuildContext buildContext;

	@Parameter(required = true, property = "project")
	protected MavenProject project;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException
	{
		performIntegrityChecks();
		File packageDir = createOutputDirectories();

		try
		{
			long maxSourceDate = vakkentabel.lastModified();

			// use atomic long for effectively final wrapper around long variable
			AtomicLong maxOutputDate = new AtomicLong(Long.MIN_VALUE);

			Files.walkFileTree(outputDirectory.toPath(), new SimpleFileVisitor<>()
			{
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
						throws IOException
				{
					if (Files.isRegularFile(file))
					{
						maxOutputDate.updateAndGet(t -> Math.max(t, file.toFile().lastModified()));
					}
					return FileVisitResult.CONTINUE;
				}
			});

			if (getLog().isDebugEnabled())
			{
				getLog().debug("max source file date: " + maxSourceDate + ", max output date: "
					+ maxOutputDate.get());
			}

			if (maxSourceDate <= maxOutputDate.get())
			{
				getLog()
					.info("no source file(s) change(s) detected! Processor task will be skipped");
				return;
			}
		}
		catch (IOException e)
		{
			getLog().warn("Exception tijdens het bepalen van de lastModified: " + e);
			// behandel deze situatie als dat er geen update controle heeft plaatsgevonden
		}

		if (buildContext == null || !buildContext.isIncremental()
			|| buildContext.hasDelta(vakkentabel))
		{
			try
			{
				getLog().info(String.format("Analyseren van %s", vakkentabel.getName()));
				Scanner scanner = new Scanner().scan(vakkentabel);

				getLog().info(String.format("%d vakregels gevonden", scanner.getAantalRegels()));

				getLog()
					.info(String.format("Genereer %s", packagePrefix.concat(".CentraalExamen")));
				scanner.generateCentraalExamen(packageDir, packagePrefix);

				getLog()
					.info(String.format("Genereer %s", packagePrefix.concat(".BeroepsgerichtVak")));
				scanner.generateBeroepsgerichtVak(packageDir, packagePrefix, matcherClass,
					matcherFactory);

				getLog().info(String.format("Genereer %s", packagePrefix.concat(".SchoolExamen")));
				scanner.generateSchoolExamen(packageDir, packagePrefix);

				getLog().info(String.format("Genereer %s", packagePrefix.concat(".OVG")));
				scanner.generateOVGVakken(packageDir, packagePrefix);

				getLog().info("Vakkentabel voltooid");
			}
			catch (IOException e)
			{
				throw new MojoFailureException("IO exception bij het scannen van de vakkentabel",
					e);
			}
			finally
			{
				if (buildContext != null)
				{
					buildContext.refresh(packageDir);
				}
			}
		}

	}

	@NotNull
	private File createOutputDirectories() throws MojoFailureException
	{
		if (!outputDirectory.exists() && !outputDirectory.mkdirs())
		{
			throw new MojoFailureException(
				"Kon doelmap target/generated-sources/vakkentabel niet aanmaken");
		}

		project.addCompileSourceRoot("target/generated-sources/vakkentabel");

		String[] pkg = packagePrefix.split("\\.");

		File packageDir = outputDirectory;

		if (pkg[0].length() > 0)
		{
			packageDir = new File(outputDirectory, pkg[0]);
		}

		if (pkg.length > 1)
		{
			for (int i = 1; i < pkg.length; i++)
			{
				packageDir = new File(packageDir, pkg[i]);
			}
		}

		if (!packageDir.exists() && !packageDir.mkdirs())
		{
			throw new MojoFailureException(String.format("Kon package %s in %s niet aanmaken",
				packagePrefix, outputDirectory.getPath()));
		}

		return packageDir;
	}

	private void performIntegrityChecks() throws MojoExecutionException, MojoFailureException
	{
		getLog().debug("Checking project");
		if (project == null)
		{
			throw new MojoExecutionException("INTERNE FOUT: Ontbrekende maven project reference");
		}
		getLog().debug(String.format("OK: %s", project.getName()));

		if (buildContext == null)
		{
			throw new MojoExecutionException("INTERNE FOUT: Ontbrekende build context");
		}

		Set<String> missing = new HashSet<>();

		getLog().debug("Checking config");

		if (vakkentabel == null)
		{
			missing.add("vakkentabel");
		}
		if (packagePrefix == null)
		{
			missing.add("packagePrefix");
		}
		if (outputDirectory == null)
		{
			missing.add("outputDirectory");
		}

		if (!missing.isEmpty())
		{
			throw new MojoFailureException(
				String.format("Ontbrekende configuratie-opties: %s", String.join(", ", missing)));
		}

		getLog().debug(String.format("- vakkentabel: %s", vakkentabel.getName()));
		getLog().debug(String.format("- packagePrefix: %s", packagePrefix));

		if (!vakkentabel.exists())
		{
			throw new MojoFailureException(
				String.format("Vakkentabel %s bestaat niet", vakkentabel.getName()));
		}

		getLog().debug("Vakkentabel exists");
	}

}
