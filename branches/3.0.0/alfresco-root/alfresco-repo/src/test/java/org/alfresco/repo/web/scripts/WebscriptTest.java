package org.alfresco.repo.web.scripts;


import org.alfresco.model.ContentModel;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.PropertyMap;
import org.alfresco.web.scripts.TestWebScriptServer.Response;
import org.alfresco.web.scripts.TestWebScriptServer.GetRequest;

public class WebscriptTest extends BaseWebScriptTest {

	  private AuthenticationService authenticationService;
	  private PersonService personService;

	  private static final String USER_ONE = "RunAsOne";

	  private static final String URL_GET_CONTENT = "/someco/test";
	  
	  
	  protected void setUp() throws Exception
	  {
	      super.setUp();

	      this.authenticationService = (AuthenticationService) getServer().getApplicationContext().getBean(
	              "AuthenticationService");
	      this.personService = (PersonService) getServer().getApplicationContext().getBean("PersonService");

	      // Create users
	      createUser(USER_ONE);
	  }

	  private void createUser(String userName)
	  {
	      if (this.authenticationService.authenticationExists(userName) == false)
	      {
	          this.authenticationService.createAuthentication(userName, "PWD".toCharArray());

	          PropertyMap ppOne = new PropertyMap(4);
	          ppOne.put(ContentModel.PROP_USERNAME, userName);
	          ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
	          ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
	          ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
	          ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");

	          this.personService.createPerson(ppOne);
	      }
	  }

	  protected void tearDown() throws Exception
	  {
	      super.tearDown();
	  }

	  public void testRunAs() throws Exception
	  {
	      Response response = sendRequest(new GetRequest(URL_GET_CONTENT), 200, "admin");
	      assertEquals(USER_ONE, response.getContentAsString());
	  }

	}
