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

import org.alfresco.maven.mmt.archiver.AmpArchiver;
import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import java.io.File;

/**
 * Build a AMP from the current project.
 *
 * @version $Id:$
 * @goal amp
 * @phase package
 * @requiresProject
 * @threadSafe
 * @requiresDependencyResolution runtime
 */
public class AmpMojo extends AbstractMojo {
    /**
     * Name of the generated JAR.
     *
     * @parameter alias="jarName" expression="${jar.finalName}" default-value="${project.build.finalName}"
     * @required
     */
    private String finalName;

    /**
     * Directory containing the classes and resource files that should be packaged into the JAR.
     *
     * @parameter default-value="${project.build.outputDirectory}"
     * @required
     */
    private File classesDirectory;

    /**
     * Classifier to add to the artifact generated. If given, the artifact will be attached.
     * If this is not given,it will merely be written to the output directory
     * according to the finalName.
     *
     * @parameter
     */
    private String classifier;

    /**
     * The Maven project.
     *
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @parameter default-value="${session}"
     * @readonly
     * @required
     */
    private MavenSession session;

    /**
     * The archive configuration to use.
     * See <a href="http://maven.apache.org/shared/maven-archiver/index.html">Maven Archiver Reference</a>.
     *
     * @parameter
     */
    private MavenArchiveConfiguration archive = new MavenArchiveConfiguration();

    /**
     * @component
     */
    private MavenProjectHelper projectHelper;

    /**
     * Generates the AMP.
     *
     */
    public File createArchive()
            throws MojoExecutionException {
        File jarFile = getJarFile(
                this.classesDirectory.getParentFile(),
                this.finalName,
                this.classifier);

        MavenArchiver archiver = new MavenArchiver();
        archiver.setArchiver( new AmpArchiver() );
        archiver.setOutputFile( jarFile );

        try {
            if (!this.classesDirectory.exists()) {
                getLog().warn( "AMP will be empty - no content was marked for inclusion!" );
            } else {
                archiver.getArchiver().addDirectory( this.classesDirectory, new String[]{}, new String[]{} );
            }
            archiver.createArchive( this.session, this.project, this.archive );
        } catch (Exception e) {
            throw new MojoExecutionException( "Error assembling AMP", e );
        }
        return jarFile;
    }

    protected static File getJarFile( File basedir, String finalName, String classifier ) {
        if ( classifier == null ) {
            classifier = "";
        } else if ( classifier.trim().length() > 0 && !classifier.startsWith( "-" ) ) {
            classifier = "-" + classifier;
        }
        return new File( basedir, finalName + classifier + ".amp" );
    }

    /**
     * Generates the AMP.
     */
    public void execute()
            throws MojoExecutionException {
        File ampFile = createArchive();
        if ( this.classifier != null ) {
            this.projectHelper.attachArtifact( this.project, "amp", this.classifier, ampFile );
        } else {
            this.project.getArtifact().setFile( ampFile );
        }
    }
}