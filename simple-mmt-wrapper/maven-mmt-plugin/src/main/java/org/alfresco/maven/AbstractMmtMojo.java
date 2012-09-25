package org.alfresco.maven;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.security.Permission;

import org.alfresco.repo.module.tool.ModuleManagementTool;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Abstract class for goals which wraps MMT Java code to manage an AMP into a given target WAR
 *
 */
public abstract class AbstractMmtMojo extends AbstractMojo
{
    protected static final String COMMAND_ARGUMENT_DELIMITER = " ";
    protected static final int SUCCESS_EXIT_CODE = 0;
    
    /**
     * The Maven project
     * 
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;
    
    /**
     * The target WAR file or exploded directory of the installation
     *
     * @parameter expression="${warFile}"
     * @required
     */
    private String warFile;
    
    /**
     * The Maven Project property the MMT output is pushed into
     * 
     * @parameter expression="${outputPropertyName}" default-value="mmtOutput"
     * @required
     */
    private String outputPropertyName;
    
    public MavenProject getProject()
    {
        return project;
    }

    public void setProject(MavenProject project)
    {
        this.project = project;
    }

    public String getWarFile()
    {
        return warFile;
    }

    public void setWarFile(String warFile)
    {
        this.warFile = warFile;
    }

    public String getOutputPropertyName()
    {
        return outputPropertyName;
    }

    public void setOutputPropertyName(String outputPropertyName)
    {
        this.outputPropertyName = outputPropertyName;
    }

    /**
     * The MMT command that should be run, i.e. install, list, etc.
     * 
     * @return the base MMT command
     * @see <a href="http://wiki.alfresco.com/wiki/Module_Management_Tool">Alfresco MMT Wiki Page</a>
     */
    protected abstract String getCommandLine();

    public void execute() throws MojoExecutionException
    {
        String commandLine = getCommandLine();
        // Preserve original system security and output
        SecurityManager origSecurityManager = System.getSecurityManager();
        PrintStream origSystemErr = System.err;
        PrintStream origSystemOut = System.out;
        ByteArrayOutputStream systemErr = new ByteArrayOutputStream();
        ByteArrayOutputStream systemOut = new ByteArrayOutputStream();
        try {
            // Redirect system security and output
            System.setSecurityManager(new CatchExitsSecurityManager());
            System.setErr(new PrintStream(systemErr));
            System.setOut(new PrintStream(systemOut));
            
            if (this.getLog().isDebugEnabled())
            {
                this.getLog().debug("Executing MMT command: " + commandLine);
            }
            
            ModuleManagementTool.main(commandLine.split(COMMAND_ARGUMENT_DELIMITER));
        } catch (CaughtExitException e) {
            String mmtOutput = new String(systemOut.toByteArray());
            String error = new String(systemErr.toByteArray());
            // Restore system security and output before logging
            System.setSecurityManager(origSecurityManager);
            System.setErr(origSystemErr);
            System.setOut(origSystemOut);
            
            project.getProperties().put(outputPropertyName, mmtOutput);
            if (e.exitCode == SUCCESS_EXIT_CODE)
            {
                this.getLog().info("MMT completed successfully:\n" + 
                        getCommandLine() + "\n" + 
                        mmtOutput);
            }
            else
            {
                this.getLog().info("MMT failed:\n" + 
                        getCommandLine() + "\n" + 
                        mmtOutput);
                throw new MojoExecutionException(error, e);
            }
        } finally {
            // Restore system security and output in case we got here by other means
            System.setSecurityManager(origSecurityManager);
            System.setErr(origSystemErr);
            System.setOut(origSystemOut);
        }
    }
    
    /**
     * SecurityException which holds the exit code
     */
    protected static class CaughtExitException extends SecurityException 
    {
        public final int exitCode;
        public CaughtExitException(int exitCode) 
        {
            super();
            this.exitCode = exitCode;
        }
    }
    
    /**
     * SecurityManager which traps <code>System.exit</code> calls
     */
    protected static class CatchExitsSecurityManager extends SecurityManager 
    {
        @Override
        public void checkPermission(Permission perm) {}
        
        @Override
        public void checkPermission(Permission perm, Object context) {}
        
        @Override
        public void checkExit(int status) 
        {
            super.checkExit(status);
            throw new CaughtExitException(status);
        }
    }

}
