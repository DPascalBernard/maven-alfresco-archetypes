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

import java.util.Arrays;
import java.util.List;

/**
 * Adds project's files and folders as build resources, so that the AMP packaging will automatically include them into
 * the build; by default
 * <p/>
 * <code>src/main/java</code> is compiled and copied into a jar in lib in the amp target folder
 * <code>src/main/amp</code> is copied into the root amp target folder
 * <code>src/main/webapp</code> is copied into the web amp target folder
 * <code>src/main/config</code> is copied into <code>alfresco/module/${project.artifactId}<code>
 * <p/>
 * You can also override the default settings by overriding the following POM properties
 * <p/>
 * <configuration>
 *   <ampBuildDirectory>${project.build.directory}/amp</ampBuildDirectory>
 *   <ampSourceDirectory>src/main/amp</ampSourceDirectory>
 *   <webappDirectory>src/main/webapp</webappDirectory>
 *   <configDirectory>src/main/config</configDirectory>
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
     * Directory containing module.properties, file-mapping.properties, licenses
     * and any other files and directories you want mapped directly to ampBuildDirectory's root
     *
     * @parameter default-value="src/main/amp"
     * @required
     */
    private String ampSourceDirectory;
    
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
     * The comma separated list of tokens to apply property expansion filtering to 
     * when copying the content of the ampSourceDirectory.
     * 
     * Note that this will also be add to the exclude for unfiltered resources.
     *
     * @parameter default-value="module.properties,file-mapping.properties"
     */
    private String ampSourceFilteredIncludes;
    
    /**
     * The comma separated list of tokens to include without filtering
     * when copying the content of the ampSourceDirectory.
     *
     * @parameter
     */
    private String ampSourceUnfilteredIncludes;
    
    /**
     * The comma separated list of tokens to exclude
     * when copying the content of the ampSourceDirectory.
     *
     * @parameter
     */
    private String ampSourceExcludes;
    
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
        List<String> ampSourceFilteredIncludesList = null;
        List<String> ampSourceUnfilteredIncludesList = null;
        List<String> ampSourceExcludesList = null;
        
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
        if (this.ampSourceFilteredIncludes != null) {
            ampSourceFilteredIncludesList = Arrays.asList(ampSourceFilteredIncludes.split(","));
        }
        if (this.ampSourceUnfilteredIncludes != null) {
            ampSourceUnfilteredIncludesList = Arrays.asList(ampSourceUnfilteredIncludes.split(","));
        }
        if (this.ampSourceExcludes != null) {
            ampSourceExcludesList = Arrays.asList(ampSourceExcludes.split(","));
        }
        
        Resource filteredAmpSourceResource = new Resource();
        filteredAmpSourceResource.setDirectory(this.ampSourceDirectory);
        if (ampSourceFilteredIncludesList != null) {
            filteredAmpSourceResource.setIncludes(ampSourceFilteredIncludesList);
        }
        if (ampSourceExcludesList != null) {
            filteredAmpSourceResource.setExcludes(ampSourceExcludesList);
        }
        filteredAmpSourceResource.setFiltering(true);
        filteredAmpSourceResource.setTargetPath(this.ampBuildDirectory);
        
        List<String> ampSourceUnfilteredExcludesList = ampSourceFilteredIncludesList;
        if (ampSourceUnfilteredExcludesList != null && ampSourceExcludesList != null) {
            ampSourceUnfilteredExcludesList.addAll(ampSourceExcludesList);
        }
        Resource unfilteredAmpSourceResource = new Resource();
        unfilteredAmpSourceResource.setDirectory(this.ampSourceDirectory);
        if (ampSourceUnfilteredIncludesList != null) {
            unfilteredAmpSourceResource.setIncludes(ampSourceUnfilteredIncludesList);
        }
        if (ampSourceUnfilteredExcludesList != null) {
            unfilteredAmpSourceResource.setExcludes(ampSourceUnfilteredExcludesList);
        }
        unfilteredAmpSourceResource.setFiltering(false);
        unfilteredAmpSourceResource.setTargetPath(this.ampBuildDirectory);
        
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
        configResource.setTargetPath(this.ampBuildDirectory + "/config/alfresco/module/" + this.artifactId);

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

        this.project.getBuild().getResources().add(filteredAmpSourceResource);
        getLog().info(String.format("Added Resource to current project: %s", filteredAmpSourceResource));
        this.project.getBuild().getResources().add(unfilteredAmpSourceResource);
        getLog().info(String.format("Added Resource to current project: %s", unfilteredAmpSourceResource));
        this.project.getBuild().getResources().add(configResource);
        getLog().info(String.format("Added Resource to current project: %s", configResource));
        this.project.getBuild().getResources().add(webappResource);
        getLog().info(String.format("Added Resource to current project: %s", webappResource));
    }
}