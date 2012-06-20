package org.alfresco.maven.mmt;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.Arrays;

/**
 * Adds project's files and folders as build resources, so that the AMP packaging will automatically include them into
 * the build; by default
 * <p/>
 * src/main/java is compiled and copied into the root amp target folder
 * module.properties is copied into the root amp target folder
 * src/main/webapp is copied into the root amp target folder
 * src/main/config is copied into alfresco/module/${project.artifactId}
 * <p/>
 * You can also override the default settings by overriding the following POM properties
 * <p/>
 * <configuration>
 * <classesDirectory>${project.build.outputDirectory}</classesDirectory>
 * <webappDirectory>src/main/webapp</webappDirectory>
 * <configDirectory>src/main/config</configDirectory>
 * </configuration>
 *
 * @author Maurizio Pillitu
 * @version $Id:$
 * @goal add-resources
 * @phase validate
 * @requiresProject
 * @threadSafe
 */
public class AddResourcesMojo extends AbstractMojo {

    /**
     * Current version of the project
     *
     * @parameter expression="${project.artifactId}"
     * @required
     */
    private String artifactId;

    /**
     * Directory containing the classes and resource files that should be packaged into the JAR.
     *
     * @parameter default-value="${project.build.outputDirectory}"
     * @required
     */
    private File classesDirectory;

    /**
     * Directory containing the web-root files
     *
     * @parameter default-value="src/main/webapp"
     * @required
     */
    private String webappDirectory;

    /**
     * Directory containing the AMP config files
     *
     * @parameter default-value="src/main/config"
     * @required
     */
    private String configDirectory;

    /**
     * The Maven project.
     *
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * Add the following resources to the project in order
     * to be filtered and copied over:
     * - module.properties
     * - src/main/config
     */
    public void execute()
            throws MojoExecutionException {

        Resource modulePropertiesResource = new Resource();
        modulePropertiesResource.setDirectory(".");
        modulePropertiesResource.setIncludes(Arrays.asList(new String[]{"module.properties"}));
        modulePropertiesResource.setFiltering(true);
        modulePropertiesResource.setTargetPath(this.classesDirectory.getAbsolutePath());

        Resource configResource = new Resource();
        configResource.setDirectory(this.configDirectory);
        configResource.setFiltering(true);
        configResource.setTargetPath(this.classesDirectory.getAbsolutePath() + "/config/alfresco/module/" + this.artifactId);

        Resource webappResource = new Resource();
        webappResource.setDirectory(this.webappDirectory);
        webappResource.setFiltering(false);
        webappResource.setTargetPath(this.classesDirectory.getAbsolutePath());

        this.project.getBuild().getResources().add(modulePropertiesResource);
        getLog().info("Added module.properties as filtered resource of current project");
        this.project.getBuild().getResources().add(configResource);
        getLog().info(String.format("Added %s as filtered resource of current project", this.configDirectory));
        this.project.getBuild().getResources().add(webappResource);
        getLog().info(String.format("Added %s as non filtered resource of current project", this.webappDirectory));
    }
}