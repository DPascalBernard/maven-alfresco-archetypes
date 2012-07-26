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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

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
 *   <classesDirectory>${project.build.outputDirectory}</classesDirectory>
 *   <webappDirectory>src/main/webapp</webappDirectory>
 *   <configDirectory>src/main/config</configDirectory>
 *   <resourceDirectory>src/main/config</resourceDirectory>
 *   <configIncludes></configIncludes>
 *   <configExcludes></configExcludes>
 *   <webappIncludes></webappIncludes>
 *   <webappExcludes></webappExcludes>
 *   <resourceIncludes></resourcesIncludes>
 *   <resourceExcludes></resourcesExcludes>
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
     * Directory containing addition resources to be added to classpath (e.g. Alfresco well known resources) 
     *
     * @parameter default-value="src/main/resources"
     * @required
     */
    private String resourceDirectory;

    /**
     * The Maven project.
     *
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
    * The comma separated list of tokens to include when copying the content
    * of the configDirectory.
    *
    * @parameter
    */
    private String configIncludes;

    /**
     * The comma separated list of tokens to exclude when copying the content
     * of the configDirectory (in alfresco/module/moduleName).
     *
     * @parameter
     */
    private String configExcludes;

    /**
     * The comma separated list of tokens to include when copying the content
     * of the webappDirectory.
     *
     * @parameter
     */
    private String webappIncludes;

    /**
     * The comma separated list of tokens to exclude when copying the content
     * of the webappDirectory.
     *
     * @parameter
     */
    private String webappExcludes;
    
    /**
     * The comma separated list of tokens to include when copying the content
     * of the configDirectory/ well known locations. Only alfresco/extension and alfresco/web-extension 
     * are added here
     *
     * @parameter default-value="alfresco/extension/*,alfresco/web-extension/*"
     */
    private String resourceIncludes;

    /**
     * The comma separated list of tokens to exclude when copying the content
     * of the webappDirectory.
     *
     * @parameter
     */
    private String resourceExcludes;

    /**
     * Add the following resources to the project in order
     * to be filtered and copied over:
     * - module.properties
     * - src/main/config to AMP/config/alfresco/module/moduleName
     * - src/main/webapp to AMP/web
     * - src/main/resources to AMP/config (default includes: alfresco/extension,alfresco/web-extension for well-known locations)
     */
    public void execute()
            throws MojoExecutionException {

        List<String> configIncludesList = null;
        List<String> configExcludesList = null;
        List<String> webappIncludesList = null;
        List<String> webappExcludesList = null;
        List<String> resourceIncludesList = null;
        List<String> resourceExcludesList = null;
        
        if (this.configIncludes != null) {
            configIncludesList = Arrays.asList(configIncludes.split(","));
        }
        if (this.configExcludes != null) {
            configExcludesList = Arrays.asList(configExcludes.split(","));
        }
        if (this.webappIncludes != null) {
            webappIncludesList = Arrays.asList(webappIncludes.split(","));
        }
        if (this.webappExcludes != null) {
            webappExcludesList = Arrays.asList(webappExcludes.split(","));
        }
        
        if (this.resourceIncludes != null) {
            resourceIncludesList = Arrays.asList(resourceIncludes.split(","));
        }
        if (this.resourceExcludes != null) {
            resourceExcludesList = Arrays.asList(resourceExcludes.split(","));
        }
        // module.properties
        // TODO: Add file-mapping.properties (override generated one in case)
        Resource modulePropertiesResource = new Resource();
        modulePropertiesResource.setDirectory(".");
        modulePropertiesResource.setIncludes(Arrays.asList(new String[]{"module.properties"}));
        modulePropertiesResource.setFiltering(true);
        modulePropertiesResource.setTargetPath(this.classesDirectory.getAbsolutePath());
        
        // Alfresco module config
        Resource configResource = new Resource();
        configResource.setDirectory(this.configDirectory);
        configResource.setFiltering(true);

        if (configIncludesList != null) {
            configResource.setIncludes(configIncludesList);
        }
        if (configExcludesList != null) {
            configResource.setExcludes(configExcludesList);
        }
        configResource.setTargetPath(this.classesDirectory.getAbsolutePath() + "/config/alfresco/module/" + this.artifactId);

        // Alfresco well known resources locations
        Resource resourceResource = new Resource();
        resourceResource.setDirectory(this.resourceDirectory);
        resourceResource.setFiltering(true);

        if (configIncludesList != null) {
            resourceResource.setIncludes(resourceIncludesList);
        }
        if (configExcludesList != null) {
            resourceResource.setExcludes(resourceExcludesList);
        }
        resourceResource.setTargetPath(this.classesDirectory.getAbsolutePath() + "/config");

        // Alfresco web resources
        Resource webappResource = new Resource();
        webappResource.setDirectory(this.webappDirectory);
        webappResource.setFiltering(false);
        if (webappIncludesList != null) {
            webappResource.setIncludes(webappIncludesList);
        }
        if (webappExcludesList != null) {
            webappResource.setExcludes(webappExcludesList);
        }
        webappResource.setTargetPath(this.classesDirectory.getAbsolutePath() + "/web");

        this.project.getBuild().getResources().add(modulePropertiesResource);
        getLog().info("Added module.properties as filtered resource of current project; includes ");
        this.project.getBuild().getResources().add(configResource);
        getLog().info(String.format("Added %s as filtered resource of current project; includes: %s ; excludes: %s", configDirectory,configIncludesList, configExcludesList));
        this.project.getBuild().getResources().add(webappResource);
        getLog().info(String.format("Added %s as non filtered resource of current project; includes: %s ; excludes: %s", webappDirectory,webappIncludesList, webappExcludesList));
        this.project.getBuild().getResources().add(resourceResource);
        getLog().info(String.format("Added %s as filtered resource of current project; includes: %s ; excludes: %s", resourceDirectory,resourceIncludesList, resourceExcludesList));
    }
}