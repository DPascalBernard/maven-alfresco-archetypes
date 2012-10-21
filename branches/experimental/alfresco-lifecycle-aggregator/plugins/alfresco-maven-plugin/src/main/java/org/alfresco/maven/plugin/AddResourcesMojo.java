package org.alfresco.maven.plugin;

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
 * @goal add-amp-resources
 * @phase generate-resources
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
     * @parameter default-value="../${project.artifactId}-${project.version}"
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
     * The comma separated list of tokens to include property expansion filtering to 
     * when copying the content of the ampSourceDirectory.
     * 
     * @parameter default-value="**"
     */
    private String ampSourceIncludes;
    
    
    /**
     * The comma separated list of tokens to exclude
     * when copying the content of the ampSourceDirectory.
     *
     * @parameter default-value=""
     */
    private String ampSourceExcludes;
    
    /**
     * Whether AMP sources (from ${ampSourceDirectory}) should be filtered. By default filtering is enabled.
     *
     * @parameter default-value="true"
     */
    private boolean filtering;
    
    /**
     * Add the following resources to the project in order
     * to be filtered and copied over:
     * - module.properties
     * - src/main/amp to AMP/ (so expects and copies over src/main/amp/config/, src/main/amp/web, etc.)
     * - src/main/resources to the AMP generated JAR
     */
    public void execute()
            throws MojoExecutionException {

        List<String> ampSourceIncludesList = null;
        List<String> ampSourceExcludesList = null;
        
        if (this.ampSourceIncludes != null) {
            ampSourceIncludesList = Arrays.asList(ampSourceIncludes.split(","));
        }

        if (this.ampSourceExcludes != null) {
            ampSourceExcludesList = Arrays.asList(ampSourceExcludes.split(","));
        }
        
        Resource ampSourceResource = new Resource();
        ampSourceResource.setDirectory(this.ampSourceDirectory);
        ampSourceResource.setFiltering(filtering);
        
        if (ampSourceIncludesList != null) {
            ampSourceResource.setIncludes(ampSourceIncludesList);
        }
        if (ampSourceExcludesList != null) {
            ampSourceResource.setExcludes(ampSourceExcludesList);
        }
        ampSourceResource.setTargetPath(this.ampBuildDirectory);
        
        this.project.getBuild().getResources().add(ampSourceResource);
        getLog().info(String.format("Added Resource to current project: %s", ampSourceResource));
        
    }
}