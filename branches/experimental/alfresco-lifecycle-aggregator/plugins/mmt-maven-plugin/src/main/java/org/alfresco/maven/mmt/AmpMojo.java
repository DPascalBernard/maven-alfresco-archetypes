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
import org.apache.commons.io.FileUtils;
import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import java.io.*;

/**
 * Build a AMP from the current project delegating the File creation to AmpArchiver;
 * the AMP file name is processed by this Mojo execution, taking in consideration
 * the classifier and the version (skipping -SNAPSHOT suffix, if needed)
 *
 * @author Gabriele Columbro, Maurizio Pillitu
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
     * @parameter alias="ampName" expression="${amp.finalName}" default-value="${project.build.finalName}"
     * @required
     */
    protected String finalName;

    /**
     * Current version of the project
     *
     * @parameter expression="${project.version}"
     * @required
     */
    protected String version;

    /**
     * The separator used to identify and strip the suffix
     *
     * @parameter expression="${separator}" default-value="-"
     * @required
     */
    protected String separator;

    /**
     * Directory containing the classes and resource files that should be packaged into the JAR.
     *
     * @parameter default-value="${project.build.outputDirectory}"
     * @required
     */
    protected File classesDirectory;

    /**
     * Classifier to add to the artifact generated. If given, the artifact will be attached.
     * If this is not given,it will merely be written to the output directory
     * according to the finalName.
     *
     * @parameter
     */
    protected String classifier;

    /**
     * The Maven project.
     *
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * @parameter default-value="${session}"
     * @readonly
     * @required
     */
    protected MavenSession session;

    /**
     * The archive configuration to use.
     * See <a href="http://maven.apache.org/shared/maven-archiver/index.html">Maven Archiver Reference</a>.
     *
     * @parameter
     */
    protected MavenArchiveConfiguration archive = new MavenArchiveConfiguration();

    /**
     * @component
     */
    protected MavenProjectHelper projectHelper;

    public void execute()
            throws MojoExecutionException {

        /**
         * We need to avoid SNAPSHOT since Alfresco only allows numeric versions
         * for AMP, otherwise will fail to load the module.
         * See issue https://issues.alfresco.com/jira/browse/ENH-1232
         */
        String noSnapshotVersion = getNoSnapshotVersion();
        if (noSnapshotVersion != this.version) {
            getLog().info("Removing SNAPSHOT suffix from version");
            File moduleFile = new File(this.classesDirectory, "module.properties");
            File bckModuleFile = new File(this.classesDirectory, "module.properties.bck");
            replace(
                    this.version,
                    noSnapshotVersion,
                    moduleFile,
                    bckModuleFile);
            getLog().info("module.properties successfully patched");
        }

        /**
         * Create the archive
         */
        File ampFile = createArchive();
        if (this.classifier != null) {
            this.projectHelper.attachArtifact(this.project, "amp", this.classifier, ampFile);
        } else {
            this.project.getArtifact().setFile(ampFile);
        }
    }

    /**
     * Creates and returns the AMP archive, invoking the AmpArchiver
     * @return a File pointing to an existing AMP package, contained
     * in ${project.build.outputDirectory}
     */
    protected File createArchive()
            throws MojoExecutionException {
        File ampFile = getAmpFile(
                this.classesDirectory.getParentFile(),
                this.finalName,
                this.classifier);

        MavenArchiver archiver = new MavenArchiver();
        archiver.setArchiver(new AmpArchiver());
        archiver.setOutputFile(ampFile);

        try {
            if (!this.classesDirectory.exists()) {
                getLog().warn("AMP will be empty - no content was marked for inclusion!");
            } else {
                archiver.getArchiver().addDirectory(this.classesDirectory, new String[]{}, new String[]{});
            }
            archiver.createArchive(this.session, this.project, this.archive);
        } catch (Exception e) {
            throw new MojoExecutionException("Error assembling AMP", e);
        }
        return ampFile;
    }

    /**
     * Check project's version; if it's a SNAPSHOT, replace and return it
     * with a no-snapshot version.
     * @return the current project's version, skipped from '-' char onwards
     */
    protected String getNoSnapshotVersion() {
        int separatorIndex = version.indexOf(separator);
        String noSnapshotVersion = version;
        if (separatorIndex > -1) {
            noSnapshotVersion = version.substring(0, separatorIndex);
        }
        return noSnapshotVersion;
    }

    /**
     * Builds a File object pointing to the target AMP package; the pointer to the File is created taking into
     * account the (optional) artifact classifier defined
     * @param basedir the Base Directory of the currently built project
     * @param finalName the Final Name of the artifact being built
     * @param classifier the optional classifier of the artifact being built
     * @return a File object pointing to the target AMP package
     */
    protected static File getAmpFile(File basedir, String finalName, String classifier) {
        if (classifier == null) {
            classifier = "";
        } else if (classifier.trim().length() > 0 && !classifier.startsWith("-")) {
            classifier = "-" + classifier;
        }
        return new File(basedir, finalName + classifier + ".amp");
    }

    /**
     * Patches given file <b>in</b>, replacing every occurrency of <b>oldstring</b> with
     * <b>newstring</b>; the result is stored in File <b>out</b>
     * @param oldstring the String to replace
     * @param newstring the String that replaces every instance of <b>oldstring</b>
     * @param in the given input File
     * @param out the given output File
     * @throws MojoExecutionException
     */
    protected static void replace(String oldstring, String newstring, File in, File out) throws MojoExecutionException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(in));
            PrintWriter writer = new PrintWriter(new FileWriter(out));
            String line = null;
            while ((line = reader.readLine()) != null)
                writer.println(line.replaceAll(oldstring, newstring));

            // I'm aware of the potential for resource leaks here. Proper resource
            // handling has been omitted in the interest of brevity
            reader.close();
            writer.close();

            //Replace in file with out
            FileUtils.copyFile(out, in);
            out.delete();
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException("Cannot find module.properties");
        } catch (IOException e) {
            throw new MojoExecutionException("Error writing to module.properties");
        }
    }
}