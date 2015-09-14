# Introduction #

This page defines the target use cases for the archetypes/plugins defined in this project. This also defines what is supported/needs to be tested upon new releases of this project.

# Core use cases (functional requirements) #

  1. run customized alfresco WAR embedded with Jetty and H2
  1. run AMP against a pre-defined Alfresco.WAR embedded with Jetty and H2
  1. allow Alfresco WAR and Share WAR to depend on AMPs/JARs, and run WARs with modules embedded in Jetty + H2
  1. package AMP and distribute to maven repository
  1. package Share JAR modules and distribute to maven repository


## Stretch goals ##

  1. run against mysql
  1. deploy to running tomcat
  1. use jrebel to enable rapid application development
  1. by default, share custom WAR should work with Alfresco custom WAR running in jetty (no port conflict, and proper share-config-custom.xml config)
  1. provide a way use the SOLR search subsystem (for Alfresco 4.x+)
  1. embedded repository scenario (FirstFoundationClient like). should be possible with the advent of the POMs

# Non functional requirements #

  1. being able to reference / switch alfresco version with a maven property
  1. support for SCM (svn and git to start with)
  1. clean / lean archetypes and fixed dependencies / plugins coming from parent pom
  1. support for community / enterprise
  1. setup test cases to validate (at least) core user stories