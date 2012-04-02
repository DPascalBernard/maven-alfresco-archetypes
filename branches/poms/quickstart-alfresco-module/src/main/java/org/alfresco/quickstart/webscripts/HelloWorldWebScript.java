package org.alfresco.quickstart.webscripts;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;


public class HelloWorldWebScript extends DeclarativeWebScript {

	protected Logger logger = Logger.getLogger(this.getClass());

	private String message;

	// @Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		logger.debug("HelloWorldWebScript");

		Map<String, Object> returnMap = new HashMap<String, Object>();
		
		returnMap.put("message", message);

		return returnMap;
	}

	// @Override
	protected void executeFinallyImpl(WebScriptRequest req, Status status, Cache cache, Map<String, Object> model) {

	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}


}
