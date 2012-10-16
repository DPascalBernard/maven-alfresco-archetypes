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
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.jar.JarArchiver;

import java.io.*;
import java.util.List;
import java.util.Set;

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
	
    private static final String AMP_LIB_FOLDER = "lib";
    private static final String AMP_CONFIG_FOLDER = "config";
	private static final String AMP_WEB_FOLDER = "web";
	private static final String AMP_LICENSES_FOLDER = "web";

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
     * The snapshotSuffix used to identify and strip the -SNAPSHOT version suffix
     * See issue https://issues.alfresco.com/jira/browse/ENH-1232
     *
     * @parameter expression="${snapshotSuffix}" default-value="-SNAPSHOT"
     * @required
     */
    protected String snapshotSuffix;

    /**
     * Enable this option in order to replace -SNAPSHOT with the currentTimestamp
     * of the artifact creation
     * See issue https://issues.alfresco.com/jira/browse/ENH-1232
     *
     * @parameter expression="${snapshotToTimestamp}" default-value="false"
     * @required
     */
    protected boolean snapshotToTimestamp;

    /**
     * Allows to append a custom (numeric) value to the current artifact's version,
     * i.e. appending the SCM build number can be accomplished defining
     * <customVersionSuffix>${buildnumber}</customVersionSuffix> in the plugin
     * configuration.
     *
     * @parameter expression="${customVersionSuffix}"
     */
    protected String customVersionSuffix;

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
     * @parameter default-value="${project.build.directory}/amp"
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

        String normalizedVersion = getNormalizedVersion();
        if (normalizedVersion != this.version) {
        	File moduleFile = new File(this.ampBuildDirectory, "module.properties");
            File bckModuleFile = new File(this.ampBuildDirectory, "module.properties.bck");
            replace(
                    this.version,
                    normalizedVersion,
                    moduleFile,
                    bckModuleFile);
            getLog().info("module.properties successfully patched; replaced " + this.version + " with " + normalizedVersion);
        }
        
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
                new File(this.ampBuildDirectory, AMP_LIB_FOLDER),
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
            	ampArchiver.getArchiver().addDirectory(this.ampBuildDirectory, new String[]{AMP_LIB_FOLDER + "/**", AMP_CONFIG_FOLDER + "/**", "*.properties" , AMP_WEB_FOLDER + "/**", AMP_LICENSES_FOLDER + "/**"}, new String[]{});
            	ampArchiver.createArchive(this.session, this.project, this.archive);
            }
            catch (Exception e) {
                throw new MojoExecutionException("Error creating AMP", e);
        	}
        }
        return ampFile;
    }

    /**
     * Normalizes the project's version following 2 patterns
     * - Remove the -SNAPSHOT suffix, if present
     * - (Optionally) append the timestamp to the version, if -SNAPSHOT is present
     * - (Optionally) append the build number to the version
     *
     * @return the current project's version normalized
     */
    protected String getNormalizedVersion() {
        int separatorIndex = version.indexOf(snapshotSuffix);
        String normalizedVersion = version;
        if (separatorIndex > -1) {
            normalizedVersion = version.substring(0, separatorIndex);
            getLog().info("Removed -SNAPSHOT suffix from version - " + normalizedVersion);
        }
        if (this.customVersionSuffix != null && this.customVersionSuffix.length() > 0) {
            normalizedVersion += "." + this.customVersionSuffix;
            getLog().info("Added custom suffix to version - " + normalizedVersion);
        } else if (this.snapshotToTimestamp) {
            normalizedVersion += "." + System.currentTimeMillis();
            getLog().info("Added timestamp to version - " + normalizedVersion);
        }
        return normalizedVersion;
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
     * @param transitive
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
    			if ( "jar".equals( type ) || "ejb".equals( type ) || "ejb-client".equals( type ) || "test-jar".equals( type ) )
                {
    				File targetFile = new File(ampBuildDirectory + File.separator + AMP_LIB_FOLDER + File.separator + artifact.getFile().getName());
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