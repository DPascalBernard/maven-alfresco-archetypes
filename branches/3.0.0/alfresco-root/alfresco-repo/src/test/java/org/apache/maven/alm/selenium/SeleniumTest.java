package org.apache.maven.alm.selenium;

import com.thoughtworks.selenium.DefaultSelenium;

import junit.framework.TestCase;


public class SeleniumTest
    extends TestCase
{
    private DefaultSelenium selenium;

    private String baseUrl;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
        String hostname = System.getProperty( "webapp.hostname" );
        String port = System.getProperty( "webapp.port" );
        String context = System.getProperty( "webapp.context" );
        baseUrl = "http://" + hostname + ":" + port;
        selenium = createSeleniumClient( baseUrl );
        selenium.start();
    }

    @Override
    public void tearDown()
        throws Exception
    {
        selenium.stop();
        super.tearDown();
    }

    protected DefaultSelenium createSeleniumClient( String url )
        throws Exception
    {
        String browser = System.getProperty( "browser" );
        String port = System.getProperty( "selenium.port" );
        return new DefaultSelenium( "localhost", Integer.parseInt( port ), browser, url );
    }

    public void testLoginPage()
        throws Exception
    {
        selenium.open(baseUrl +  "/alfresco-repo-3.0.0-SNAPSHOT/faces/jsp/login.jsp" );
        assertTrue( selenium.isTextPresent( "Alfresco Web Client - Login" ) );
    }
}