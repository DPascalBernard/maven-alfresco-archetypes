
Overview
--------

This Maven plugin wraps the 
[Alfresco Module Management Tool](http://wiki.alfresco.com/wiki/Module_Management_Tool) 
Java code to facilitate the management of AMPs in WARs.

It uses the same command line format and options available when using the MMT directly
and intercepts that tool's exit codes and `System.out` which is available by default
in the `mmtOutput` property after execution.

The goals are executed in the pre-integration-test phase.


Install Goal
------------

Use the `install` goal to apply a specified `ampFile` to the specified `warFile`
using any `options` specified.

For example:

    <plugin>
        <artifactId>maven-mmt-plugin</artifactId>
        <groupId>org.alfresco.maven</groupId>
        <version>0.1-SNAPSHOT</version>
        <executions>
            <execution>
                <id>apply-amp-repo</id>
                <phase>pre-integration-test</phase>
                <goals>
                    <goal>install</goal>
                </goals>
                <configuration>
                    <ampFile>${project.build.directory}/${project.artifactId}-${project.version}.amp</ampFile>
                    <warFile>${WAR_FILE_REPO}</warFile>
                </configuration>
            </execution>
        </executions>
    </plugin>


List Goal
---------

The `list` goal will return the details about all the modules currently installed in the 
`warFile` specified.

For example:

    <plugin>
        <artifactId>maven-mmt-plugin</artifactId>
        <groupId>org.alfresco.maven</groupId>
        <version>0.1-SNAPSHOT</version>
        <executions>
            <execution>
                <id>list-amps-repo</id>
                <phase>pre-integration-test</phase>
                <goals>
                    <goal>list</goal>
                </goals>
                <configuration>
                    <warFile>${WAR_FILE_REPO}</warFile>
                </configuration>
            </execution>
        </executions>
    </plugin>

Output of the command is stored in the `mmtOutput` property by default.

