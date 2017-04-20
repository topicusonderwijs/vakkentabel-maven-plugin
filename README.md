# vakkentabel-maven-plugin

(english translation below)

N.B: Vanaf versie 1.3 moeten de ingelezen csv een puntkomma (;) als scheidingsteken hebben. In vorige versies was dit een komma (,).

Dit is een maven plugin om een set aan util/helper klassen te maken op basis van de DUO vakkentabel (http://www.duo.nl/zakelijk/klantenservice/softwareleveranciers/Programmas_van_eisen.asp). Deze klassen bevatten methoden als `public static boolean isBeroepsgerichtVak(int opleidingIlt, int vakIlt)`.

De plugin is gebouwd voor Maven3 en geschikt voor zowel commandline uitvoer als m2e.

Zie hieronder wat je moet toevoegen in je pom.xml om deze plugin te gebruiken

Releasen
=========

* mvn release:prepare 
* mvn release:perform 

# English translation

This is a maven plugin to convert Dutch educational information from DUO to util classes which answer such questions as whether or not a subject is required for a specific curriculum.

It probably won't be of use for non-Dutch except as an example of how to create a source-generating maven plugin. This plugin should work from the commandline as well as integrate with m2e. See below for how to configure your pom to use this plugin.

# Example usage (pom.xml)

```
<build>
  <pluginManagement>
    <plugins>
      <plugin>
        <groupId>nl.topicus.onderwijs.vakkentabel</groupId>
        <artifactId>vakkentabel-maven-plugin</artifactId>
        <version>0.9-SNAPSHOT</version>
        <executions>
          <execution>
            <id>vakkentabel</id>
            <goals>
              <goal>generate</goal>
            </goals>
          </execution>
        </executions>
        <configuration>
          <vakkentabel>src/main/resources/vakkentabel.csv</vakkentabel>
          <packagePrefix>org.example.util.vakkentabel</packagePrefix>
          <matcherClass>org.example.VakMatcher</matcherClass>
          <matcherFactory>org.example.MatcherFactory</matcherFactory>
          <outputDirectory>${project.build.directory}/generated-sources/vakkentabel</outputDirectory>
        </configuration>
      </plugin>
    </plugins>
  </pluginManagement>
</build>

<profiles>
	<profile>
		<id>codegen</id>
		<build>
			<plugins>
				<plugin>
					<groupId>nl.topicus.onderwijs.vakkentabel</groupId>
					<artifactId>vakkentabel-maven-plugin</artifactId>
				</plugin>
			</plugins>
	</build>
</profile>
```
