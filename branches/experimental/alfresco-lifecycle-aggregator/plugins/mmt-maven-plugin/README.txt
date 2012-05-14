***************
What does it do
***************

The mmt-maven-plugin provides the following features:

* Packages an AMP starting from a simple (and configurable) Maven project folder structure
* Performs AMP to WAR overlay by using the Alfresco Repository ModuleManagementTool and emulating the same process
during Alfresco boostrap


*****
Usage
*****

+ In order to build an AMP file, you must:
----

1. Define your POM as <packaging>amp</packaging>

2. Specify a module.properties file in the project's root folder, containing the following properties:
module.id=${project.artifactId}
module.title=${project.name}
module.description=${project.description}
module.version=${project.version}
As you can see, the file is filtered with Maven project placeholders

3. Declare the mmt-maven-plugin in your POM build section

    <build>
        <plugins>
            <plugin>
                <groupId>org.alfresco.maven.plugin</groupId>
                <artifactId>mmt-maven-plugin</artifactId>
                <version>0.1-SNAPSHOT
            </plugin>
        </plugins>
        ...
    </build>

+ In order to overlay an existing Alfresco WAR file, you'll need the following elements:
----

1. A WAR dependency to the Alfresco webapp:

    <dependencies>
        <dependency>
            <groupId>org.alfresco.enterprise</groupId>
            <artifactId>alfresco</artifactId>
            <version>4.0.1</version>
            <type>war</type>
        </dependency>
    </dependencies>

2. An mmt-plugin configuration to run the install goal after the AMP have been packaged

    <plugin>
        <groupId>org.alfresco.maven.plugin</groupId>
        <artifactId>mmt-maven-plugin</artifactId>
        <version>0.1-SNAPSHOT</version>
        <executions>
            <execution>
                <id>unpack-amps</id>
                <phase>package</phase>
                <goals>
                    <goal>install</goal>
                </goals>
                <configuration>
                    <singleAmp>${project.build.directory}/${project.build.finalName}.${project.packaging}</singleAmp>
                </configuration>
            </execution>
        </executions>
    </plugin>
