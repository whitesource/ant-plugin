package org.whitesource.agent.ant.plugin;

/**
 * Constants used by ant task. 
 * 
 * @author tom.shapira
 *
 */
public final class Constants {
	
	/* --- Ant plugin --- */ 
	
	public static final String AGENT_TYPE = "ant-plugin";
	
	public static final String AGENT_VERSION = "1.0";
	
	public static final String SHA1 = "SHA-1";
	
	/* --- Error messages --- */

	public static final String ERROR_SHA1 = "Error calculating SHA-1";

	public static final String ERROR_PATH = "Path not set";

	public static final String ERROR_PROJECT_IDENTIFIER = "Project name / token not set";
	
	/* --- Log messages --- */
	
	public static final String DEBUG_REQUEST_BUILT = "Request created successfully";
	
	/* --- Constructors --- */
	
	/**
	 * Private default constructor
	 */
	private Constants() {
		// avoid instantiation
	}

}