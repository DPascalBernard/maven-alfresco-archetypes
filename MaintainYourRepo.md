# Introduction #

This is useful to _unofficially_ maintain Alfresco artifact versions on a corporate Maven repository (Enterprise artifacts can also be managed). This is used as a temporary measure to feed [Alfresco Community Maven Repository](http://maven.alfresco.com) as Alfresco itself is **not** building with Maven.

# How to deploy a new Alfresco Version #

In order to have a new version deployed in your repo you can download the Alfresco Community / Enterprise War distribution and after unpacking them with the following layout:
```
Base Folder
      - alfresco.war
      - alfresco (unzipped alfresco.war)
      - share.war 
      - share (unzipped share.war)
```
You can run the attached a bash script to upload all the required JARs/WARs in the defined repository.

The script m2-bootstrap.sh is located here:
  * [For Alfresco 3.2.x](http://maven-alfresco-archetypes.googlecode.com/files/m2-bootstrap.sh)
  * [For Alfresco 3.3 (HEAD)](http://maven-alfresco-archetypes.googlecode.com/files/m2-bootstrap-33plus.sh)

Script synopsis is documented in the comments of the script but for completeness a typical command line looks like:

./m2-bootstrap.sh /unpacking/folder/ alfresco-releases http://maven.alfresco.com/nexus/content/repositories/snapshot  3.3-SNAPSHOT community 3.3dev

Where parameters mean:
  1. Base folder where Alfresco distro was unpacked as detailed earlier
  1. Id of the repository to deploy to (has to match with ~/.m2/settings.xml authentication settings if relevant)
  1. Url of the deployment repository
  1. Version to use to deploy the artifacts
  1. Classifier to be appended to the artifacts (community|enterprise)
  1. Alfresco Internal Version (suffix of JARs inside WEB-INF/lib, typically same as parameter 4 for releases, different   for snapshots)


# Improvement Ideas #

A fully automated script from Alfresco trunk could:
  * build Alfresco
  * copy the the Ant built release WARs in SVN checkout `cp root/projects/*/build/dist/*.war to $TMPFOLDER`
  * `unzip *.war -d $TMPFOLDER`
  * `mvn deploy *.war in $TMPFOLDER`
  * `mvn deploy $TMPFOLDER/*/WEB-INF/lib/alfresco-*.jar`


# Notes #

This approach can be used both for Community and Enterprise artifacts (in case you're a subscribing client), in case you want to manage your Alfresco lifecycle with Maven.

**Limitation**: This approach provides no POMs (as there are none ATM) so you'll have to flatten your dependency tree in order to be able to build and test extensions based on Alfresco runtime libraries.

# Unofficial Alfresco POMs #

A new attempt to maintain Alfresco POMs is to be found here see https://github.com/hubick/alfresco_sdk_mvn_deploy