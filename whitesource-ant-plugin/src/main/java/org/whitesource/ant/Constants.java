package org.whitesource.ant;

/**
 * Constants used by ant task. 
 * 
 * @author tom.shapira
 *
 */
public final class Constants {
	
	/* --- Ant plugin --- */ 
	
	public static final String AGENT_TYPE = "ant-task";
	
	public static final String AGENT_VERSION = "1.0";
	
	/* --- Error messages --- */

	public static final String ERROR_SHA1 = "Error calculating SHA-1";

	public static final String ERROR_PATH = "Path not set";

	public static final String ERROR_PROJECT_IDENTIFIER = "Project name / token not set";
	
	public static final String ERROR_MISSING_API_KEY = "Missing API Key";
	
	public static final String ERROR_CHECK_POLICIES_COUNT = "There should be only one check policies element";
	
	public static final String ERROR_CHECK_POLICIES_OUTPUT = "Missing output directory for policies report";
	
	public static final String ERROR_UPDATE = "A problem occurred while updating projects: ";
	
	public static final String ERROR_CHECK_POLICIES = "A problem occurred while checking policies: ";
	
	public static final String ERROR_CHECK_POLICIES_REPORT = "Error generating policies report";
	
	/* --- Log messages --- */
	
	public static final String DEBUG_REQUEST_BUILT = "Request created successfully";

	public static final String POLICIES_REJECTIONS = "Some rejections were found, review report for details";

	public static final String POLICIES_REPORT_GENERATED = "Policies report generated successfully";

	public static final String RESULT_NO_NEW_PROJECTS = "No new projects found";

	public static final String RESULT_NEW_PROJECTS = "Newly created projects:";

	public static final String RESULT_NO_UPDATES = "No projects were updated";

	public static final String RESULT_UPDATES = "Updated projects:";

	public static final String RESULT_DOMAIN = "Inventory update results for ";
	
	/* --- Constructors --- */
	
	/**
	 * Private default constructor
	 */
	private Constants() {
		// avoid instantiation
	}

}