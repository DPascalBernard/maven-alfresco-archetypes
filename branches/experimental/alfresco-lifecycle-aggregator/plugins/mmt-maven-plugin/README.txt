The mmt-maven-plugin provides the following features:

* embeds an Alfresco Repositoory POJO (ModuleManagementTool) which performs AMP to WAR overlay
* packages an AMP by using JAR layout

Since Alfresco Repository relies on truezip a JAR dependency which cannot be found on any public
Maven repository (version 5.1.2), it is necessary to install it locally:

mvn install:install-file -Dfile=truezip.jar -DgroupId=de.schlichtherle.truezip -DartifactId=truezip -Dversion=5.1.2 -Dpackaging=jar