package org.alfresco.maven;

/**
 * Goal which wraps MMT Java code to list AMPs in a given target WAR
 *
 * @goal list
 * @phase pre-integration-test
 */
public class MmtListMojo extends AbstractMmtMojo
{
    protected static final String COMMAND_LIST = "list";
    
    @Override
    protected String getCommandLine()
    {
        return COMMAND_LIST + COMMAND_ARGUMENT_DELIMITER + 
                getWarFile();
    }

}
