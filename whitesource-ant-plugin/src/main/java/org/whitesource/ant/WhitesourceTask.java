package org.whitesource.ant;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.ProxySetup;
import org.whitesource.agent.api.ChecksumUtils;
import org.whitesource.agent.api.dispatch.CheckPoliciesResult;
import org.whitesource.agent.api.dispatch.UpdateInventoryResult;
import org.whitesource.agent.api.model.AgentProjectInfo;
import org.whitesource.agent.api.model.Coordinates;
import org.whitesource.agent.api.model.DependencyInfo;
import org.whitesource.api.client.WhitesourceService;
import org.whitesource.api.client.WssServiceException;

/**
 * Sends an update request to White Source.
 * 
 * @author tom.shapira
 *
 */
public class WhitesourceTask extends Task {

	/* --- Property members --- */

	/**
	 * Unique identifier of the organization with White Source.
	 */
	private String apiKey;

	/**
	 * Project name to be matched with White Source project name.
	 */
	private String projectName;

	/**
	 * Project token to match White Source project.
	 */
	private String projectToken;

	/**
	 * Paths to folders and files to update.
	 */
	private Vector<Path> paths;

	/**
	 * Whether or not to stop the build when encountering an error.
	 */
	private boolean stopBuild;

	/**
	 * Check policies nested element.
	 */
	private Vector<CheckPolicies> checkPolicies = new Vector<CheckPolicies>();

	/**
	 * Url to send requests (debug proposes).
	 */
	private String wssurl;

	/* --- Members --- */

	private Set<File> fileToUpdate;

	private boolean shouldCheckPolicies;

	private CheckPolicies policyCheck;

	private AgentProjectInfo projectInfo;

	private WhitesourceService service;

	/* --- Overridden Ant Task methods --- */

	@Override
	public void init() throws BuildException {
		super.init();

		paths = new Vector<Path>();
		stopBuild = true;
		fileToUpdate = new HashSet<File>();
		shouldCheckPolicies = false;
	}

	@Override
	public void execute() throws BuildException {
		validate();

		createWhitesourceService();

		initProjectInfo();

		getAllFiles();

		collectDependencies();

		sendRequest();
	}

	/* --- Private methods --- */

	private void createWhitesourceService() {
		service = new WhitesourceService(Constants.AGENT_TYPE, Constants.AGENT_VERSION, wssurl);

		// set proxy information
        Properties systemProperties = System.getProperties();
        String proxyHost = systemProperties.getProperty(ProxySetup.HTTP_PROXY_HOST);
        String proxyPort = systemProperties.getProperty(ProxySetup.HTTP_PROXY_PORT);
        String proxyUsername = systemProperties.getProperty(ProxySetup.HTTP_PROXY_USERNAME); // optional
        String proxyPassword = systemProperties.getProperty(ProxySetup.HTTP_PROXY_PASSWORD); // optional
        
        // check if proxy is enabled
        if (!StringUtils.isBlank(proxyHost) &&
        		!StringUtils.isBlank(proxyPort)) {
        	service.getClient().setProxy(
        			proxyHost,
        			Integer.parseInt(proxyPort),
        			proxyUsername,
        			proxyPassword);
        }
	}

	private void sendRequest() {
		// check policies
		if (shouldCheckPolicies) {
			checkPolicies();
		}

		try {
			UpdateInventoryResult result = service.update(apiKey, Arrays.asList(projectInfo));
			logResult(result);
		} catch (WssServiceException e) {
			error(Constants.ERROR_UPDATE + e.getMessage());
		}
	}

	private void logResult(UpdateInventoryResult result) {
		log(Constants.RESULT_DOMAIN + result.getOrganization());

		// newly created projects
		Collection<String> createdProjects = result.getCreatedProjects();
		if (createdProjects.isEmpty()) {
			log(Constants.RESULT_NO_NEW_PROJECTS);
		} else {
			log(Constants.RESULT_NEW_PROJECTS);
			for (String projectName : createdProjects) {
				log(projectName);
			}
		}

		// updated projects
		Collection<String> updatedProjects = result.getUpdatedProjects();
		if (updatedProjects.isEmpty()) {
			log(Constants.RESULT_NO_UPDATES);
		} else {
			log(Constants.RESULT_UPDATES);
			for (String projectName : updatedProjects) {
				log(projectName);
			}
		}
	}

	private void checkPolicies() {
		try {
			CheckPoliciesResult result = service.checkPolicies(apiKey, Arrays.asList(projectInfo));
			generatePoliciesReport(result);
			if (/*checkPoliciesResult.hasRejections() &&*/ // TODO remove comment when available
					policyCheck.isFailonrejection()) {
				throw new BuildException(Constants.POLICIES_REJECTIONS);
			}
		} catch (WssServiceException e) {
			error(Constants.ERROR_CHECK_POLICIES + e.getMessage());
		}
	}

	private void generatePoliciesReport(CheckPoliciesResult result) {
		try {
			ReportGenerator reportGenerator = new ReportGenerator();
			reportGenerator.generatePolicyRejectionsReport(result, policyCheck.getReportdir());
			log(Constants.POLICIES_REPORT_GENERATED, Project.MSG_INFO);
		} catch (IOException e) {
			error(Constants.ERROR_CHECK_POLICIES_REPORT);
		}
	}

	private void collectDependencies() {
		// calculate SHA-1 for all files
		for (File file : fileToUpdate) {
			try {
				String sha1 = ChecksumUtils.calculateSHA1(file);
				log("SHA-1 for '" + file.getName() + "' = " + sha1);
				
				// add dependency to project info
				projectInfo.getDependencies().add(new DependencyInfo(sha1));
			} catch (IOException e) {
				log("Problem calculating SHA-1 for '" + file.getName() + "'", e, Project.MSG_DEBUG);
			}
		}
	}

	private void getAllFiles() {
		// get all included files
		for (Path path : paths) {
			for (String includedFile : path.list()) {
				fileToUpdate.add(new File(includedFile));
			}
		}
	}

	private void initProjectInfo() {
		projectInfo = new AgentProjectInfo();

		if (StringUtils.isBlank(apiKey)) {
			projectInfo.setProjectToken(projectToken);
		} else {
			// set project name as maven artifactId coordinate
			projectInfo.setCoordinates(new Coordinates(null, projectName, null));
		}
	}

	private void validate() {
		// api key
		if (StringUtils.isBlank(apiKey)) {
			error(Constants.ERROR_MISSING_API_KEY);
		}

		// paths
		if (paths.size() < 1) {
			error(Constants.ERROR_PATH);
		}

		// project name / token
		if (StringUtils.isBlank(projectName) && 
				StringUtils.isBlank(projectToken)) {
			error(Constants.ERROR_PROJECT_IDENTIFIER);
		}

		// check policies
		if (!checkPolicies.isEmpty()) {
			shouldCheckPolicies = true;
			validateCheckPolicies();
		}
	}

	private void validateCheckPolicies() {
		policyCheck = checkPolicies.iterator().next();

		if (checkPolicies.size() > 1) {
			error(Constants.ERROR_CHECK_POLICIES_COUNT);
		} else {
			File reportdir = policyCheck.getReportdir();
			if (reportdir == null || !reportdir.exists()) {
				error(Constants.ERROR_CHECK_POLICIES_OUTPUT);
			}
		}
	}

	private void error(String errorMsg) {
		if (stopBuild) {
			throw new BuildException(errorMsg);
		} else {
			log(errorMsg, Project.MSG_ERR);
		}
	}

	/* --- Property set methods --- */

	public void setProjectname(String projectname) {
		this.projectName = projectname;
	}

	public void setStopbuild(boolean stopbuild) {
		this.stopBuild = stopbuild;
	}

	public void addPath(Path path) {
		this.paths.add(path);
	}

	public void setProjecttoken(String projecttoken) {
		this.projectToken = projecttoken;
	}

	public void setApikey(String apikey) {
		this.apiKey = apikey;
	}

	public void addCheckpolicies(CheckPolicies checkpolicies) {
		this.checkPolicies.add(checkpolicies);
	}

	public void setWssurl(String wssurl) {
		this.wssurl = wssurl;
	}

}
