Disclaimer
---
This codebase is currently work in progress; the effort have been split in two directions:
a) the definition of 2 POM files that can handle versions and common build behaviors/features
b) the definition of an all-in-one quickstart with nearly no customizations, mostly focused on
environment configuration and build process.

Most of the build features have been successfully ported to a new structure, whose main advantage is
to keep pom.xml files extremely simple and readable (since a lot of logic have been moved to the parent).
Inherited behaviors are configurable simply defining specific properties in the project's or module's pom.xml
(see documentation below).

Here follows a list of build features that have not been ported yet:
* Maven SCM and release
* JBoss run
* Tomcat remote deployment
* Jetty Java source and resources reloading (Jetty currently runs .war files)
* Maven Site generation
* Maven reporting
* Maven distributionManagement

Apart from SCM and release, all the mentioned features are probably best suited for a more enterprise archetype,
using the Alfresco Web Integration POM as parent and adding more build features around Maven, Tomcat and JBoss.

Regarding the POM files, I'd like to see them deployed on maven.alfresco.com, hopefully generated (by the
Alfresco build?) and tested for each Alfresco release.


Introduction
---
The quickstart-alfresco-integration archetype aims to show a clean and simple Alfresco
SDK based on Maven in order to implement an Alfresco foundation project composed by
Repository, Share and one AMP module.

The project is composed by the following modules:

* amp - an AMP module containing a Java Demo component
* alfresco - an Alfresco Repository/Explorer Extension which provides a custom alfresco-config.properties
and depends on the sibling amp module
* share - an Alfresco Share Client that connects to a local Alfresco Repository and depends on the sibling
amp module
* runner - a module that runs Jetty with alfresco and share modules deployed on separate contexts

The parent pom.xml - besides defining the list of sub-modules and some of its properties - inherits
(and propagates) different configurations from alfresco-specific parent pom:

The alfresco-integration-parent POM (the parent POM of alfresco-web-integration-parent) specifies many
versions and common plugin configurations:
- Maven pluginManagement fixes versions and common configurations (compiler, war, resources plugin);
can be overridden via properties; no plugin versions should be necessary in project's sub-modules
- Maven dependencyManagement fixes versions and scope of Alfresco (and third-party) artifacts; no
dependency versions should be necessary in project's sub-modules

The alfresco-web-integration-parent POM (the parent POM of this project) provides some cool features
that saves a lot of code in the current project's build, such as:
- No DB installation needed; Alfresco Repository - by default - runs using H2 embedded DB; there is
no need to tweak DB configuration
- Maven clean plugin removes any trace of your Alfresco runs (be careful with mvn clean)
- Alfresco Extensions are automatically overlayed with all their AMP dependencies, with no need of
Maven code
- Multi-environment property filtering with no Maven configuration hassle
- Maven Jetty plugin can run Alfresco and Share on the same server using few lines of configuration

Run
---
Simply type (from the project's root folder)

MAVEN_OPTS="-Xms256m -Xmx1G -XX:PermSize=300m" mvn install -Prun

The following services will start:
- http://localhost:8080/alfresco
- http://localhost:8080/share

   --- oOo ---

POM files

---
FEATURES - alfresco-integration-parent
---
Activation: built-in

* DependencyManagement for all Alfresco commonly used and WAR artifacts.
* PluginManagement for all Maven plugin commonly used

-- Properties

<alfresco.version>4.0.b</alfresco.version>
<alfresco.edition>community</alfresco.edition>


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

<env>local</env>
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
