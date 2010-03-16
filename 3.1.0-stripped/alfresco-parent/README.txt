NOTE!!!
the archetype is not yet in pla e!
this is a normal alfresco customization project that aims to be the base on which the archetypes 3.1.0 will be build upon

svn co http://maven-alfresco-archetypes.googlecode.com/svn/branches/3.1.0-stripped/alfresco-parent/



CREATING THE PROJECT

--------------------
mvn archetype:create -DarchetypeGroupId=org.alfresco.maven -DarchetypeArtifactId=alfresco-archetype -DarchetypeVersion=0.5 -DgroupId=it.session.maven -DartifactId=alfresco-parent -Dversion=3.1.0-beta-1
cd alfresco-parent


Edit build-time properties of each submodule
--------------------------------------------
vi profiles.xml

Edit runtime environment-related properties of each submodule
-------------------------------------------------------------
vi alfresco-*/src/main/resources/alfresco/extension/application.properties

(Optional) Alfresco third-party tools
------------------------------
sudo apt-get install swftools
sudo apt-get install imagemagick

FEATURES:
---------

MySQL Support (-P mysql)
------------------------
dev@mybox ~/ $ mysql -u root -p
mysql> create database alfresco;
mysql> grant all privileges on alfresco.* to 'alfresco'@'localhost' identified by 'alfresco';

  
TODO
----
- properties are not resolved at runtime in custom-repository-context.xml
- test alfresco-share
- test mysql feature
- test tomcat feature
- include restore feature
- include ldap feature