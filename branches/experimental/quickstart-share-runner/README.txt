Introduction
---
This archetype aims to show a clean and simple Alfresco SDK based on Maven in order to implement
an Alfresco foundation project composed by Repository and Share.

The project is fully configurable via the (project's root) pom.xml, specifically its properties; the rest of the Maven code
is not meant to changed and hopefully will be soon inherited by some Alfresco Parent POMs (see comments inline in pom.xml)

Biggest efforts we aimed to
* one command to run it all
* all embedded (no external components nor configuration needed)
* keep pom.xml files readable

Run it
---
To run quickstart-alfresco-integration, simply type (from the project's root folder)

MAVEN_OPTS="-Xms256m -Xmx1G -XX:PermSize=300m" mvn install -Prun

The following services will start:
- http://localhost:8080/alfresco
- http://localhost:8080/share

Specs
---
The DB in use is H2 embedded (thanks @skuro), therefore you don't need to setup a DB by your own.


---
FEATURES - alfresco-web-integration-parent
---


* Alfresco Repository Log and storage cleaning
---
Activation: built-in

When mvn clean is invoked, all files produced by Maven runs must be removed; this is the list of
filesets inherited from alfresco-web-integration-parent:

 * target/ (default)
 * *.log
 * ${alfresco.data.location}

-- Properties

  <alfresco.data.location>alf_data_jetty</alfresco.data.location>


* Multi-environment properties placeholding
---
Activation: exists src/main/properties

You can enable multi-environment property placeholding by simply creating the
src/main/properties/${env} folder structure; in order to switch between environments,
simply attach -Denv=yourenv to your mvn commands; filter propertyfile is located in
src/main/properties/${env}/${webapp.resource.filter}

By default, filtered resources are
 * src/main/resources, copied into ${project.build.outputDirectory}/classes
 * src/main/properties, copied into ${webapp.resource.build.folder}
(@TODO - and src/main/webapp is not?)

-- Properties

  <webapp.resource.filter>alfresco-global.properties</webapp.resource.filter>
  <webapp.resource.build.folder>${project.build.outputDirectory}</webapp.resource.build.folder>
  <webapp.name>${project.artifactId}</webapp.name>


* Jetty H2 configuration
---
Activation: exists jetty/jetty-env.xml

You can enable Jetty to run your application(s); by default Jetty will run the ROOT context
using jetty/root-web.xml (defined by <jetty.root.webxml> within your pom.xml), so that
you can easily define the contextHandlers of one or more applications as follows:

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

-- Properties

  <jetty.port>8080</jetty.port>
  <jetty.root.contextpath>/</jetty.root.contextpath>
  <jetty.root.sourcedir>.</jetty.root.sourcedir>
  <jetty.root.webxml>jetty/root-web.xml</jetty.root.webxml>
