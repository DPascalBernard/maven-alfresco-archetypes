CREATING THE PROJECT
--------------------
mvn archetype:create -DarchetypeGroupId=org.alfresco.maven -DarchetypeArtifactId=alfresco-root-archetype -DarchetypeVersion=0.5 -DgroupId=it.session.maven -DartifactId=alfresco-root -Dversion=3.0.0
cd alfresco-root


Edit build-time properties of each submodule
--------------------------------------------
vi alfresco-repo/profiles.xml
vi alfresco-share/profiles.xml
vi apache-ds/profiles.xml
vi cas/profiles.xml


Edit runtime environment-related properties of each submodule
-------------------------------------------------------------
vi alfresco-repo/src/main/properties/local/application.properties
vi alfresco-share/src/main/properties/local/application.properties
vi apache-ds/src/main/properties/local/application.properties
vi cas/src/main/properties/local/application.properties

(Optional) Alfresco third-party tools
------------------------------
sudo apt-get install swftools
sudo apt-get install imagemagick

USE ALM
-------
Run either from alfresco-root(recommended) or from the root path of the submodules (i.e. alfresco-repo)

		mvn clean && mvn -P
			[local,] \
			[hsqldb|mysql] \
			[ldap,cas,liferay,] \
			[tomcat5x,tc-server,jboss4x] \
			[start-container,j2ee-deploy,integration-test,selenium-test,]

examples
--------
			+ mvn 

FEATURES:
---------

MySQL Support (-P mysql)
------------------------
dev@mybox ~/ $ mysql -u root -p
mysql> create database alfresco;
mysql> grant all privileges on alfresco.* to 'alfresco'@'localhost' identified by 'alfresco';

Ldap Support (-P ldap)
----------------------
- Install an LDAP Client (i.e ldapexplorer - http://www.mcs.anl.gov/~gawor/ldap/)
- Login using the admin credentials (Host: localhost; BaseDN: ou=system; UserDN: uid=admin,ou=system; Password: secret)
- Copy uid=admin,ou=system into uid=useradmin,ou=users,ou=system (remove the property uid=admin from the newly created entry)
- mvn -P ???
- Login on Alfresco with useradmin/secret

CAS (not tested yet)
--------------------

Liferay (not tested yet)
------------------------
(xpatch first to integrate w/ cas)


FLOWS:
------
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
  
  
Release checklist:
-------
-- Complete Selenium
-- local run/remote deploy working
-- Integrate properly liferay/cas
-- site and maven-changes-plugin
-- finalize refactor properties
-- add maven-amp-archetype
-- document a lot
-- certificate on localhost with CAS (depends on hostmane??)
-- test maven-release-plugin (write prerequisites ie SVN 1.4)
-- move tc-server (tc.server.home) into alm-parent, like tomcat5x and jboss4x
-- remove relativePath from alfresco-root to alm-parent
-- create archetypes
-- release on nexus sourcesense http://repository.sourcesense.com/nexus
  - maven archetypes
-- Test with Maven 2.0.10
-- licensing on file headers

Nice to have:
--------------
-- maven-override-properties-plugin - accepts a Java resource bundle file, a targetPath and a list of property names; copies the resource bundle into the target overriding the values of the properties provided by configuration with the value of the related maven property.
