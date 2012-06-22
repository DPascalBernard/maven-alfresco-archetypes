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

import org.alfresco.repo.module.tool.ModuleManagementTool;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Performs a AMP to WAR overlay invoking the Alfresco Repository POJO
 * ModuleManagementTool.installModules() and therefore emulating the same
 * WAR overlay performed by Alfresco Repository during bootstrap.
 * <p/>
 * The AMP files overlaid are all AMP runtime dependencies defined in the
 * current project's build.
 * <p/>
 * Optionally you can define the full path of a single AMP file that needs to
 * be overlaid, using the <simpleAmp> configuration element.
 *
 * @version $Id:$
 * @requiresDependencyResolution
 * @goal install
 */
public class InstallMojo extends AbstractMojo {

    private static final String AMP_OVERLAY_FOLDER_NAME = "ampoverlays";

    /**
     * Build directory where the AMP files get copied waiting for being
     * overlaid on top of the WAR file
     *
     * @parameter expression="${ampDestinationDir}"
     */
    private File ampDestinationDir;

    /**
     * One single amp file that, if exists, gets included into the list
     * of modules to install within the Alfresco WAR
     *
     * @parameter expression="${singleAmp}"
     */
    private File singleAmp;

    /**
     * Name of the artifact generated into target/ folder.
     *
     * @parameter expression="${project.build.finalName}"
     * @required
     */
    private String finalName;

    /**
     * The target/ directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private String outputDirectory;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    public InstallMojo() {
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (this.ampDestinationDir == null) {
            this.ampDestinationDir = new File(this.outputDirectory, AMP_OVERLAY_FOLDER_NAME);
        }
        getLog().debug("Setting AMP Destination dir to " + this.ampDestinationDir.getAbsolutePath());

        /**
         * Collect all AMP runtime dependencies and copy all files
         * in one single build folder, *ampDirectoryDir*
         */
        try {
            for (Object artifactObj : project.getRuntimeArtifacts()) {
                if (artifactObj instanceof Artifact) {
                    Artifact artifact = (Artifact) artifactObj;
                    if ("amp".equals(artifact.getType())) {
                        File artifactFile = artifact.getFile();
                        FileUtils.copyFileToDirectory(artifactFile, this.ampDestinationDir);
                        getLog().debug(String.format("Copied %s into %s", artifactFile, this.ampDestinationDir));
                    }
                }
            }
            if (this.singleAmp != null && this.singleAmp.exists()) {
                if (!this.ampDestinationDir.exists()) {
                    this.ampDestinationDir.mkdirs();
                }
                FileUtils.copyFileToDirectory(this.singleAmp, this.ampDestinationDir);
                getLog().debug(String.format("Copied %s into %s", this.singleAmp, this.ampDestinationDir));
            }
        } catch (IOException e) {
            getLog().error(
                    String.format(
                            "Cannot copy AMP module to folder %s",
                            this.ampDestinationDir));
        }

        /**
         * Locate the WAR file to overlay - the one produced by the current project
         */
        String warLocation = this.outputDirectory + '/' + this.finalName + ".war";
        File war = new File(warLocation);
        if (
                !war.exists() ||
                        this.ampDestinationDir == null ||
                        !this.ampDestinationDir.exists()) {
            getLog().info(
                    "No WAR file found in " + warLocation + " - skipping overlay.");
        } else if (this.ampDestinationDir.listFiles().length == 0) {
            getLog().info(
                    "No runtime AMP dependencies found for this build - skipping overlay.");
        } else {
            /**
             * Invoke the ModuleManagementTool to install AMP modules on the WAR file;
             * so far, no backup or force flags are enabled
             */
            ModuleManagementTool mmt = new ModuleManagementTool();
            mmt.setVerbose(true);
            mmt.installModules(
                    this.ampDestinationDir.getAbsolutePath(),
                    war.getAbsolutePath(),
                    false,  //preview
                    true,   //force install
                    false); //backup
        }
    }
}