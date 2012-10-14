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
import java.util.List;

/**
 * Adds project's files and folders as build resources, so that the AMP packaging will automatically include them into
 * the build; by default
 * <p/>
 * <code>src/main/java</code> is compiled and copied into a jar in lib in the amp target folder
 * <code>src/main/amp/module.properties</code> and <code>src/main/amp/file-mapping.properties</code> are copied into the root amp target folder
 * <code>src/main/webapp</code> is copied into the web amp target folder
 * <code>src/main/config</code> is copied into <code>alfresco/module/${project.artifactId}<code>
 * <p/>
 * You can also override the default settings by overriding the following POM properties
 * <p/>
 * <configuration>
 *   <ampBuildDirectory>${project.build.directory}/amp</ampBuildDirectory>
 *   <webappDirectory>src/main/webapp</webappDirectory>
 *   <configDirectory>src/main/config</configDirectory>
 *   <modulePropertiesFile>${project.basedir}/module.properties</modulePropertiesFile>
 *   <fileMappingPropertiesFile>${project.basedir}/file-mapping.properties</fileMappingPropertiesFile>
 *   <mapConfigToModuleTargetPath>true</mapConfigToModuleTargetPath>
 *   <configIncludes></configIncludes>
 *   <configExcludes></configExcludes>
 *   <webappIncludes></webappIncludes>
 *   <webappExcludes></webappExcludes>
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
     * Note that using ${project.build.directory}/amp results in files with an absolute path being
     * copied into target/classes as {@link Resource#setTargetPath(String)} expects a relative path within
     * target/classes.
     *
     * @parameter default-value="../amp"
     * @required
     */
    private String ampBuildDirectory;

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
     * Whether or not to map the source configDirectory.
     * If true, /config -> amp/config/alfresco/module/*artifactId*
     * else,    /config -> amp/config
     *
     * @parameter default-value="true"
     */
    private boolean mapConfigToModuleTargetPath;
    
    /**
     * The path to the module.properties file
     *
     * @parameter expression="${amp.modulePropertiesFile}" default-value="src/main/amp/module.properties"
     */
    private File modulePropertiesFile;
    
    /**
     * The path to the file-mapping.properties file
     *
     * @parameter expression="${amp.fileMappingPropertiesFile}" default-value="src/main/amp/file-mapping.properties"
     */
    private File fileMappingPropertiesFile;
    
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
        
        // module.properties may be defined outside of ampSourceDirectory
        String modulePropertiesParent = modulePropertiesFile.getPath().replaceAll("\\/module\\.properties", "");
        Resource modulePropertiesResource = new Resource();
        modulePropertiesResource.setDirectory(modulePropertiesParent);
        modulePropertiesResource.setIncludes(Arrays.asList(new String[]{"module.properties"}));
        modulePropertiesResource.setFiltering(true);
        modulePropertiesResource.setTargetPath(this.ampBuildDirectory);
        
        // file-mapping.properties may be defined outside of ampSourceDirectory
        // TODO: Synthesize file-mapping.properties if it does not exist
        String fileMappingPropertiesParent = fileMappingPropertiesFile.getPath().replaceAll("\\/file-mapping\\.properties", "");
        Resource fileMappingPropertiesResource = new Resource();
        fileMappingPropertiesResource.setDirectory(fileMappingPropertiesParent);
        fileMappingPropertiesResource.setIncludes(Arrays.asList(new String[]{"file-mapping.properties"}));
        fileMappingPropertiesResource.setFiltering(true);
        fileMappingPropertiesResource.setTargetPath(this.ampBuildDirectory);
        
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
        if (mapConfigToModuleTargetPath) {
            configResource.setTargetPath(this.ampBuildDirectory + "/config/alfresco/module/" + this.artifactId);
        } else {
            configResource.setTargetPath(this.ampBuildDirectory + "/config");
        }

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
        webappResource.setTargetPath(this.ampBuildDirectory + "/web");

        this.project.getBuild().getResources().add(modulePropertiesResource);
        getLog().info(String.format("Added Resource to current project: %s", modulePropertiesResource));
        this.project.getBuild().getResources().add(fileMappingPropertiesResource);
        getLog().info(String.format("Added Resource to current project: %s", fileMappingPropertiesResource));
        this.project.getBuild().getResources().add(configResource);
        getLog().info(String.format("Added Resource to current project: %s", configResource));
        this.project.getBuild().getResources().add(webappResource);
        getLog().info(String.format("Added Resource to current project: %s", webappResource));
    }
}