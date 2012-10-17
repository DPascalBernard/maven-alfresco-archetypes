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

import de.schlichtherle.io.File;

import java.util.Arrays;
import java.util.List;

/**
 * Adds project's files and folders as build resources, so that the AMP packaging will automatically include them into
 * the build; by default
 * <p/>
 * <code>src/main/java</code> is compiled and copied into a jar in lib in the amp target folder
 * <code>src/main/amp</code> is copied into the root amp target folder
 * 
 * <p/>
 * You can also override the default settings by overriding the following POM properties
 * <p/>
 * <configuration>
 *   <ampBuildDirectory>${project.build.directory}/amp</ampBuildDirectory>
 *   <ampSourceDirectory>src/main/amp</ampSourceDirectory>
 *   <ampSourceFilteredIncludes></ampSourceFilteredIncludes>
 *   <ampSourceUnfilteredIncludes></ampSourceUnfilteredIncludes>
 *   <ampSourceExcludes></ampSourceExcludes>
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
     * The Maven project.
     *
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * The comma separated list of tokens to apply property expansion filtering to 
     * when copying the content of the ampSourceDirectory.
     * 
     * Note that this will also be add to the exclude for unfiltered resources.
     *
     * @parameter default-value="module.properties,file-mapping.properties,config/alfresco/module/${project.artifactId}/module-context.xml,config/alfresco/module/${project.artifactId}/context/*-context.xml"
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
     * - src/main/amp to AMP/ (so expects and copies over src/main/amp/config/, src/main/amp/web, etc.)
     * - src/main/resources to the AMP generated JAR
     */
    public void execute()
            throws MojoExecutionException {

        List<String> ampSourceFilteredIncludesList = null;
        List<String> ampSourceUnfilteredIncludesList = null;
        List<String> ampSourceExcludesList = null;
        
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
        
        // The files which are by default filtered are removed from the unfiltered copy 
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
        
        this.project.getBuild().getResources().add(filteredAmpSourceResource);
        getLog().info(String.format("Added Resource to current project: %s", filteredAmpSourceResource));
        
        this.project.getBuild().getResources().add(unfilteredAmpSourceResource);
        getLog().info(String.format("Added Resource to current project: %s", unfilteredAmpSourceResource));
    }
}