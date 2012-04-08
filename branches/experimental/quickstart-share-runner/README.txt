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

To run quickstart-share-runner, simply type (from the project's root folder)

mvn package -Prun

The following services will start:
- http://localhost:8080/alfresco (still buggy)
- http://localhost:8080/share

Specs
---

One Jetty Server instance will be run on a forked JVM using the following args: -Xms256m -Xmx1G -XX:PermSize=256m
To tweak your JVM parameters, check (project's root) pom.xml <jetty.jvmArgs>

The DB in use is H2 embedded (thanks @skuro), therefore you don't need to setup a DB by your own.


---
FEATURES
---


Multi-environment properties placeholding
---

As additional feature, you can enable multi-environment property placeholding simply creating the
src/main/properties/${env} folder structure used in both alfresco and share modules; within your pom.xml, make sure that
you redefine (when defaults don't suit your needs):

  <webapp.resource.filter>alfresco-global.properties</webapp.resource.filter>
  <webapp.resource.build.folder>${project.build.outputDirectory}</webapp.resource.build.folder>
  <webapp.name>${project.artifactId}</webapp.name>

This way, you can invoke your mvn commands adding -Denv=yourenv in order to use as property filter file:

src/main/properties/yourenv/${webapp.resource.filter}

By default the folders filtered are

src/main/resources, copied into ${project.build.outputDirectory}/classes
src/main/properties, copied into ${webapp.resource.build.folder}