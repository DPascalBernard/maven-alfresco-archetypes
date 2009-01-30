Install Software
----------------
sudo apt-get install swftools
sudo apt-get install imagemagick

Preparing the DB
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

 mvn clean && mvn -P local,mysql,ldap,cas,run



FEATURES:
---------
Available profiles:
- Mysql (and hsqldb? no?)
- Ldap
- CAS (untested)
- Liferay (xpatch first to integrate w/ cas)

FLOWS:
------
- Run
