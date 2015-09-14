# Update: July 2014 - Maven Alfresco SDK migrated to Github! Thanks Google Code! #
After 6 years of honorable service we are moving this project to [Github](https://github.com/Alfresco/alfresco-sdk/), to consolidate in the Alfresco organization and foster more contribution.

All code (trunk, tags, branches) and issues have been moved already, so please abstain modifying this project and join / fork us at https://github.com/Alfresco/alfresco-sdk.

This project is **now therefore deprecated** and content will be removed soon.


# Update: November 2013 - Maven Alfresco SDK 1.1.1 is out! #

Maven Alfresco SDK 1.1.1 was released!

See full documentation [here](https://artifacts.alfresco.com/nexus/content/repositories/alfresco-docs/alfresco-lifecycle-aggregator/latest/index.html) and the [Summit 2013 presentation](http://summit.alfresco.com/barcelona/sessions/enabling-test-driven-rapid-dev-continuous-delivery-alfresco-apps) for an extended intro.

Includes:
  * Support for JRebel and rapid development
  * Support for Alfresco 4.2.e Community and 4.2.0 Enterprise
  * Remote JUnit testing, to avoid Alfresco application contexts reloading
  * Supported on Enterprise by Alfresco. See also [official docs](http://docs.alfresco.com/4.2/topic/com.alfresco.enterprise.doc/tasks/dev-extensions-maven-getting-started.html)
  * Update to Java7

# Update: November 2012 - Maven Alfresco SDK 1.0 is out! #

Maven Alfresco SDK 1.0 was released!

See full documentation [here](https://artifacts.alfresco.com/nexus/content/repositories/alfresco-docs/alfresco-lifecycle-aggregator/latest/index.html) and the [DevCon presentation](http://devcon.alfresco.com/berlin/sessions/alfresco-maven-happy-ending-or-just-beginning) for an extended intro.

Includes:
  * Support for Alfresco POMs
  * Community and Enterprise support
  * Unit testing
  * Run embedded and integration testing with Jetty and H2
  * Lots of cool stuff :)

# Update: 07/04/2012 -  Maven Alfresco Lifecycle 3.9.1 released #

See ReleasesInformation for the Docs & Release information. Binaries are hosted on maven.alfresco.com


# Project Rationale #

Alfresco is the Open Source Alternative for Enterprise Content Management (ECM), providing Document Management, Collaboration, Records Management, Knowledge Management, Web Content Management and Imaging.

Fancy working with Alfresco ECM or contributing to it, but don't want to waste way too much time in googling ?

Then, you are in the right place. Just embrace maven :)

Alfresco still bases its customizations on custom Ant builds and a hardly manageable and extensible SDK. This consideration makes it definitely not of easy penetration in two very important contexts like:

**[Enterprise processes](http://www.slideshare.net/guest67a9ba/maven-application-lifecycle-management-for-alfresco):
being ECM Alfresco should ship enterprise process level lifecycle management for its modules, with features like dependency management, properties filtering, documentation, release, centralized lib version management, etc. Managing Alfresco custom modules with Maven will provide all the mentioned benefits.**

**open source mainstream community (e.g. Apache) :
due to the convention over configuration based approach to customization, developing an extension too often ends up in infinite unstructured wiki/forum documentation lookup. Using maven archetypes and embedded Jetty appserver run you could have an Alfresco application running with just 2 command lines standard commands.**


The usage of [Maven Alfresco archetypes](http://wiki.alfresco.com/wiki/Managing_Alfresco_Lifecyle_with_Maven) can solve both this issues and bring back Alfresco development to what it should be, i.e. FUN!

So, have fun! ;)