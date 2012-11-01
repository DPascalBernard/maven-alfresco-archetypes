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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import org.alfresco.maven.plugin.archiver.AmpArchiver;
import org.apache.commons.io.FileUtils;
import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.jar.JarArchiver;

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
     * Directory containing the classes and resource files that should be packaged into the JAR.
     *
     * @parameter default-value="${project.build.outputDirectory}"
     * @required
     */
    protected File classesDirectory;

    /**
     * Directory to build the AMP in 
     *
     * @parameter default-value="${project.build.directory}/${project.artifactId}-${project.version}"
     * @required
     */
    protected File ampBuildDirectory;

    /**
     * ${project.basedir}/target directory
     *
     * @parameter default-value="${project.build.directory}"
     * @required
     */
    protected File outputDirectory;

    /**
     * Classifier to add to the artifact generated. If given, the artifact will be attached.
     * If this is not given,it will merely be written to the output directory
     * according to the finalName.
     *
     * @parameter
     */
    protected String classifier;
    

    /**
     * Whether (runtime scoped) JAR dependencies (including transitive) should be added or not to the generated AMP /lib folder. 
     * By default it's true so all direct and transitive dependencies will be added
     * 
     * @parameter default-value="true"
     * @required
     */
    protected boolean includeDependencies;


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

        if(includeDependencies)
        	gatherDependencies();
        
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
     *
     * @return a File pointing to an existing AMP package, contained
     *         in ${project.build.outputDirectory}
     */
    protected File createArchive()
            throws MojoExecutionException {
        File jarFile = getFile(
                new File(this.ampBuildDirectory, AmpModel.AMP_FOLDER_LIB),
                this.finalName,
                this.classifier,
                "jar");

        File ampFile = getFile(
                this.outputDirectory,
                this.finalName,
                this.classifier,
                "amp"
        );

        MavenArchiver jarArchiver = new MavenArchiver();
        jarArchiver.setArchiver(new JarArchiver());
        jarArchiver.setOutputFile(jarFile);

        MavenArchiver ampArchiver = new MavenArchiver();
        ampArchiver.setArchiver(new AmpArchiver());
        ampArchiver.setOutputFile(ampFile);

        if (!this.ampBuildDirectory.exists()) {
            getLog().warn("outputDirectory does not exist - AMP will be empty");
        } else {
        try {
            jarArchiver.getArchiver().addDirectory(this.classesDirectory, new String[]{}, new String[]{});
            jarArchiver.createArchive(this.session, this.project, this.archive);
        	} catch (Exception e) {
                throw new MojoExecutionException("Error creating JAR", e);
        	}
            try {                        
            	ampArchiver.getArchiver().addDirectory(this.ampBuildDirectory, new String[]{"**"}, new String[]{});
            	ampArchiver.createArchive(this.session, this.project, this.archive);
            }
            catch (Exception e) {
                throw new MojoExecutionException("Error creating AMP", e);
        	}
        }
        return ampFile;
    }

    /**
     * Builds a File object pointing to the target AMP package; the pointer to the File is created taking into
     * account the (optional) artifact classifier defined
     *
     * @param basedir    the Base Directory of the currently built project
     * @param finalName  the Final Name of the artifact being built
     * @param classifier the optional classifier of the artifact being built
     * @return a File object pointing to the target AMP package
     */
    protected static File getFile(File basedir, String finalName, String classifier, String extension) {
        if (classifier == null) {
            classifier = "";
        } else if (classifier.trim().length() > 0 && !classifier.startsWith("-")) {
            classifier = "-" + classifier;
        }
        return new File(basedir, finalName + classifier + "." + extension);
    }

    /**
     * Patches given file <b>in</b>, replacing every occurrency of <b>oldstring</b> with
     * <b>newstring</b>; the result is stored in File <b>out</b>
     *
     * @param oldstring the String to replace
     * @param newstring the String that replaces every instance of <b>oldstring</b>
     * @param in        the given input File
     * @param out       the given output File
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
            throw new MojoExecutionException("Cannot find file: " + in.getPath());
        } catch (IOException e) {
            throw new MojoExecutionException("Error writing to file: " + out.getPath());
        }
    }
    
    /**
     * Copies all runtime dependencies to AMP lib. By default transitive runtime dependencies are retrieved.
     * This behavior can be configured via the transitive parameter
     * @throws MojoExecutionException
     */
    protected void gatherDependencies() throws MojoExecutionException
    {
        Set<Artifact> dependencies = null;
        // Whether transitive deps should be gathered or not
        dependencies = project.getArtifacts();

        ScopeArtifactFilter filter = new ScopeArtifactFilter( Artifact.SCOPE_RUNTIME );
        
        for (Artifact artifact : dependencies) {
            if ( !artifact.isOptional() && filter.include( artifact ) )
            {
                String type = artifact.getType();

                if (AmpModel.EXTENSION_LIST.contains(type))
                {
                    File targetFile = new File(ampBuildDirectory + File.separator + AmpModel.AMP_FOLDER_LIB + File.separator + artifact.getFile().getName());
                    String targetFilePath = targetFile.getPath();
                    try {
                        FileUtils.copyFile(artifact.getFile(), targetFile);
                    } catch (IOException e) {
                        throw new MojoExecutionException("Error copying transitive dependency " + artifact.getId() + " to file: " + targetFilePath);
                    }
                }
            }
        }
    }
}