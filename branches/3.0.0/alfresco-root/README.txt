Install Software
----------------
sudo apt-get install swftools
sudo apt-get install imagemagick

Preparing the DB (not needed when -P hsqldb) 
----------------
dev@mint-dev ~/workspace/alm-parent-test/alm-parent $ mysql -u root -p
mysql> create database alfresco;
mysql> grant all privileges on alfresco.* to 'alfresco'@'localhost' identified by 'alfresco';

Creating the project
--------------------
mvn archetype:create -DarchetypeGroupId=it.session.maven -DarchetypeArtifactId=alm-parent-archetype -DarchetypeVersion=0.5 -DgroupId=it.session.maven -DartifactId=alm-parent -Dversion=0.6
cd alm-parent

Edit build-time properties
-----------------------------------
vi profiles.xml

Edit runtime properties
-----------------------------------
vi alfresco-repo/src/main/properties/local/application.properties


Ldap
----
- Install ApacheDS 1.5.4 and run it
- Install an LDAP Client (i.e ldapexplorer - http://www.mcs.anl.gov/~gawor/ldap/)
- Login using the admin credentials (Host: localhost; BaseDN: ou=system; UserDN: uid=admin,ou=system; Password: secret)
- Copy uid=admin,ou=system into uid=useradmin,ou=users,ou=system (remove the property uid=admin from the newly created entry)
- mvn -P local,mysql,ldap,run
- Login on Alfresco with useradmin/secret

USE
---
Run either from : alfresco-root, alfresco-repo, alfresco-share
with:

 mvn clean && mvn -P[local,]mysql[,ldap,cas,]integration-test


LAYOUT:
-------
Submodules:

- alfresco-repo
- alfresco-share

-- Behaviors can be defined at any level but should be inherited by alm-parent were possible
-- Dependencies can be defined at alfresco root level


FEATURES:
---------
Available profiles:
- Mysql (and hsqldb? no?)
- Ldap
- CAS (untested)
- Liferay (xpatch first to integrate w/ cas)

FLOWS:
------
- Run (WIP)
- integration-test (with patched version of surefire)
  - The surefire dependency is to 2.5-SNAPSHOT build which allows loading Resources in classpath to enable WEB-INF/lib seamless loading
  - This version of the plugin is hosted in  http://box.session.it:8080/nexus/content/repositories/snapshots/org/apache/maven/surefire/
  - TODO: Submit patch SurefireResourceLoading.diff to Surefire
  - A WebscriptTest is also added
  - Test on Multiapp (First repo then SHARE)
- distribute
  - Deploys to a specific appserver by configuring appropriate properties
  - Tested on Multiapplication (Repo+SHARE)
  - Tested with Remote JBOSS deploy (-Pci,mysql,test) from alfresco-root using cargo patched 1.0-SNAPSHOT available on 
  box-snapshots http://box.session.it:8080/nexus/content/repositories/snapshots/org/codehaus/cargo/)
- selenium-test
  - Runs selenium tests for every webapp under alfresco root matching the following package pattern under src/test/java:
    **/selenium/**.java
  - STILL TO TEST: selenese at single webapp level
    
 
ENVIRONMENTS:
-------------
- local
- ci (continuous integration)
  
  
TODO:
-------
-- Selenium
-- Integrate properly liferay/cas
-- site and maven-changes-plugin
-- refactor properties
-- document a lot
-- certificate on localhost with CAS (depends on hostmane??)
-- test maven-release-plugin (write prerequisites ie SVN 1.4)
-- create archetypes
-- release on nexus sourcesense http://repository.sourcesense.com/nexus
  - maven surefire 2.5 snapshot
  - cignex-sso-ldap.jar (private - check pateinting)
  - maven archetypes
  - tc-server (private - check pateinting)
  - all snapshot deps with specific classifier

-- maven-override-properties-plugin - accepts a Java resource bundle file, a targetPath and a list of property names; copies the resource bundle into the target overriding the values of the properties provided by configuration with the value of the related maven property.
