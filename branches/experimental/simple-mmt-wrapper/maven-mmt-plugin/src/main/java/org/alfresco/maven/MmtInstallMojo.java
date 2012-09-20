package org.alfresco.maven;

/**
 * Goal which wraps MMT Java code to install an AMP into a given target WAR
 *
 * @goal install
 * @phase pre-integration-test
 */
public class MmtInstallMojo extends AbstractMmtMojo
{
    protected static final String COMMAND_INSTALL = "install";
    
    /**
     * AMP file to be installed
     *
     * @parameter expression="${ampFile}" default-value="example.amp"
     * @required
     */
    private String ampFile;
    
    /**
     * The options for the MMT install command
     *
     * @parameter expression="${options}" default-value="-force -verbose -nobackup"
     * @see <a href="http://wiki.alfresco.com/wiki/Module_Management_Tool">Alfresco MMT Wiki Page</a>
     */
    private String options;
    
    public String getAmpFile()
    {
        return ampFile;
    }

    public void setAmpFile(String ampFile)
    {
        this.ampFile = ampFile;
    }
    
    public String getOptions()
    {
        return options;
    }

    public void setOptions(String options)
    {
        this.options = options;
    }

    @Override
    protected String getCommandLine()
    {
        return COMMAND_INSTALL + COMMAND_ARGUMENT_DELIMITER + 
                getAmpFile() + COMMAND_ARGUMENT_DELIMITER + 
                getWarFile() + COMMAND_ARGUMENT_DELIMITER + 
                getOptions();
    }

}
