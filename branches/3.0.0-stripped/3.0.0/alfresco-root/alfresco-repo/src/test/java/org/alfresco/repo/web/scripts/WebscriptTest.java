package org.alfresco.repo.web.scripts;

import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.web.scripts.TestWebScriptServer.GetRequest;
import org.alfresco.web.scripts.TestWebScriptServer.Response;



/**
 * Sample webscript integration test working with Maven
 * (a simplified version of http://wiki.alfresco.com/wiki/3.0_Web_Scripts_Testing)
 * @author g.columbro
 *
 */
public class WebscriptTest extends BaseWebScriptTest {

	private AuthenticationComponent authenticationComponent;
	
	private static final String ADMIN_USER = "admin";

	private static final String URL_GET_CONTENT = "/index/all";

	protected void setUp() throws Exception {
		super.setUp();
		this.authenticationComponent = (AuthenticationComponent) getServer().getApplicationContext().getBean("authenticationComponent");
		// Authenticate as user
        this.authenticationComponent.setCurrentUser(ADMIN_USER);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testWebscript() throws Exception {
		GetRequest request = new GetRequest(URL_GET_CONTENT);
		Response response = sendRequest(request, 200);
		assertEquals(200,response.getStatus());
		assertTrue(new String(response.getContentAsByteArray()).contains("Index of All Web Scripts"));
	}

}
