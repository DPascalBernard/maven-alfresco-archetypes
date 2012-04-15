Introduction
---
This archetype aims to show a clean and simple Alfresco SDK based on Maven in order to implement
an Alfresco foundation project composed by Repository, Share and one AMP module.

The project is composed by the following modules:

* amp - an AMP module containing a Java Demo component
* alfresco - an Alfresco Repository/Explorer Extension which provides a custom alfresco-config.properties
and depends on the sibling amp module
* share - an Alfresco Share Client that connects to a local Alfresco Repository and depends on the sibling
amp module
* runner - a module that runs Jetty with alfresco and share modules deployed on separate contexts

The alfresco-web-integration-parent POM (the parent POM of this project) provides some cool features
that saves a lot of code in the current project's build, such as:
- Maven pluginManagement fixes versions and common configurations (compiler, war, resources plugin);
can be overridden via properties; no plugin versions should be necessary in project's sub-modules
- Maven dependencyManagement fixes versions and scope of Alfresco (and third-party) artifacts; no
dependency versions should be necessary in project's sub-modules
- No DB installation needed; Alfresco Repository - by default - runs using H2 embedded DB; there is
no need to tweak DB configuration
- Maven clean plugin removes any trace of your Alfresco runs (be careful with mvn clean)
- Alfresco Extensions are automatically overlayed with all their AMP dependencies, with no need of
Maven code
- Multi-environment property filtering with no Maven configuration hassle
- Maven Jetty plugin can run Alfresco and Share on the same server using few lines of configuration

The project is fully configurable via the (project's root) pom.xml properties.

Run it
---
To run quickstart-alfresco-integration, simply type (from the project's root folder)

MAVEN_OPTS="-Xms256m -Xmx1G -XX:PermSize=300m" mvn install -Prun

The following services will start:
- http://localhost:8080/alfresco
- http://localhost:8080/share

   --- oOo ---

POM files

---
FEATURES - alfresco-web-integration-parent
---


* Alfresco Repository Log and storage cleaning
---
Activation: built-in

When mvn clean is invoked, all files produced by Maven runs must be removed; this is the list of
filesets inherited from alfresco-web-integration-parent:

 * target/ (default behaviour)
 * *.log
 * ${alfresco.data.location}

-- Properties

<alfresco.data.location>alf_data_jetty</alfresco.data.location>


* AMP overlay into an Alfresco (or Share) Extension
---
Activation: built-in

When your project (or sub-module) is a <packaging>war</packaging>, you can automatically include one or more
AMP files by defining the dependencies into the pom.xml, as follows:

<dependency>
    <groupId>com.mycompany</groupId>
    <artifactId>amp-module</artifactId>
    <version>1.0-SNAPSHOT</version>
    <type>amp</type>
</dependency>

The AMP files will be overlayed on top of your current WAR customizations, therefore
they can override the content of the original WAR.


* Multi-environment property filtering
---
Activation: exists src/main/properties

You can enable multi-environment property filtering by simply creating the
src/main/properties/${env}/${webapp.resource.filter} file with your property values;
all files included in src/main/resources and src/main/properties will be filtered
with your properties defined; in order to switch between environments,
simply attach -Denv=yourenv to your mvn commands.

-- Properties

<webapp.resource.filter>alfresco-global.properties</webapp.resource.filter>
<webapp.resource.build.folder>${project.build.outputDirectory}</webapp.resource.build.folder>
<webapp.name>${project.artifactId}</webapp.name>


* Jetty H2 configuration
---
Activation: exists jetty/jetty-env.xml

You can enable Jetty to run your application(s); by default Jetty will run the ROOT context
using jetty/root-web.xml, but you can easily define the contextHandlers of one or more applications
as follows:

<plugin>
    <groupId>org.mortbay.jetty</groupId>
    <artifactId>maven-jetty-plugin</artifactId>
    <executions>
        <execution>
            <id>run</id>
            <goals><goal>run</goal></goals>
            <phase>package</phase>
            <configuration>
                <contextHandlers>
                    <contextHandler implementation="org.mortbay.jetty.webapp.WebAppContext">
                        <war>${project.basedir}/../alfresco/target/alfresco.war</war>
                        <contextPath>/alfresco</contextPath>
                    </contextHandler>
                    <contextHandler implementation="org.mortbay.jetty.webapp.WebAppContext">
                        <war>${project.basedir}/../share/target/share.war</war>
                        <contextPath>/share</contextPath>
                    </contextHandler>
                </contextHandlers>
            </configuration>
        </execution>
    </executions>
</plugin>

NOTE
It is advised strongly to run your Jetty using <war> configuration,
otherwise the extensions and AMP overlays will be ignored.

-- Properties

<jetty.port>8080</jetty.port>
<jetty.root.contextpath>/</jetty.root.contextpath>
<jetty.root.sourcedir>.</jetty.root.sourcedir>
<jetty.root.webxml>jetty/root-web.xml</jetty.root.webxml>
