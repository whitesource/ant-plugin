/**
 * Copyright (C) 2012 White Source Ltd.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.whitesource.ant;

import ch.qos.logback.classic.Level;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.slf4j.Logger;
import org.whitesource.agent.api.dispatch.CheckPolicyComplianceResult;
import org.whitesource.agent.api.dispatch.UpdateInventoryResult;
import org.whitesource.agent.api.model.AgentProjectInfo;
import org.whitesource.agent.api.model.ChecksumType;
import org.whitesource.agent.api.model.Coordinates;
import org.whitesource.agent.api.model.DependencyInfo;
import org.whitesource.agent.client.WhitesourceService;
import org.whitesource.agent.client.WssServiceException;
import org.whitesource.agent.hash.ChecksumUtils;
import org.whitesource.agent.hash.HashAlgorithm;
import org.whitesource.agent.hash.HashCalculator;
import org.whitesource.agent.report.PolicyCheckReport;
import org.whitesource.ant.utils.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Sends an inventory update request to White Source.
 *
 * @author tom.shapira
 */
public class WhitesourceTask extends Task {

    /* --- Static members --- */

    public static Logger logger;

    /* --- Property members --- */

    /**
     * Log Level for reports .
     */
    private String logLevel;

    /**
     * Unique identifier of the organization with White Source.
     */
    private String apiKey;

    /**
     * Name or unique identifier of the product to update.
     */
    private String product;

    /**
     * Version of the product to update.
     */
    private String productVersion;

    /**
     * Modules to update.
     */
    private Vector<Module> modules;

    /**
     * Whether or not to stop the build when encountering an error.
     */
    private boolean failOnError;

    /**
     * Check policies configuration.
     */
    private Vector<CheckPolicies> checkPolicies;

    /**
     * White Source service url. Leave blank to use default, SaaS.
     */
    private String wssUrl;

    /**
     * Whether or not to update inventory when encountering policy violation.
     */
    private boolean forceUpdate;

	/* --- Members --- */

    private boolean shouldCheckPolicies;

    private boolean forceCheckAllDependencies;

    private CheckPolicies policyCheck;

    private Collection<AgentProjectInfo> projectInfos;

    private WhitesourceService service;

    private static final String JAVA_SCRIPT_REGEX = ".*\\.js";

	/* --- Overridden Ant Task methods --- */

    @Override
    public void init() throws BuildException {
        super.init();

        modules = new Vector<Module>();
        checkPolicies = new Vector<CheckPolicies>();
        failOnError = true;
        forceUpdate = false;
        shouldCheckPolicies = false;
        forceCheckAllDependencies = false;
        projectInfos = new ArrayList<AgentProjectInfo>();
    }

    @Override
    public void execute() throws BuildException {
        validateAndPrepare();
        scanModules();
        createService();
        try {
            checkPolicies();
        } catch (BuildException e) {
            if (forceUpdate) {
                updateInventory();
                error(e);
            }
        }
        updateInventory();
    }

	/* --- Private methods --- */

    private void validateAndPrepare() {

        setLoggerConfiguration(logLevel);

        // api key
        if (StringUtils.isBlank(apiKey)) {
            error("Missing API Key");
        }

        // product
        if (StringUtils.isBlank(product)) {
            product = getProject().getName();
        }

        // modules
        if (modules.isEmpty()) {
            Module module = new Module(); // Treat whole project as single module
            module.setName("Default Module");
            addDefaultPaths(module);
            modules.add(module);
        } else {
            int emptyModules = 0;
            for (Module module : modules) {
                if (StringUtils.isBlank(module.getName()) && StringUtils.isBlank(module.getToken())) {
                    error("Expecting module name or token");
                }
                if (module.getPaths().isEmpty()) {
                    emptyModules++;
                }
            }
            if (emptyModules > 0) {
                if (modules.size() == 1) {
                    addDefaultPaths(modules.iterator().next());
                } else {
                    error("Path not set");
                }
            }
        }

        // check policies
        if (!checkPolicies.isEmpty()) {
            shouldCheckPolicies = true;
            policyCheck = checkPolicies.iterator().next();
            forceCheckAllDependencies = policyCheck.isForcecheckalldependencies();
            File reportDir = policyCheck.getReportdir();
            if (reportDir == null) {
                reportDir = new File(this.getProject().getBaseDir(), "reports");
                policyCheck.setReportdir(reportDir);
            }
            if (!reportDir.exists() && !reportDir.mkdirs()) {
                error("Policies report directory doesn't exists and can not be created");
            }
        }
    }

    private static void setLoggerConfiguration(String logLevel) {
        logger = LoggerFactory.getLogger(WhitesourceTask.class);
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

        if (logLevel != null && logLevel.equalsIgnoreCase("debug")){
            root.setLevel(Level.toLevel(logLevel, Level.DEBUG));
        }
        else {
            root.setLevel(Level.toLevel(logLevel, Level.INFO));
        }
    }

    private void addDefaultPaths(Module module) {
        Project project = getProject();

        FileSet fs = new FileSet();
        fs.setDir(project.getBaseDir());
        List<String> includes = new ArrayList<String>();
        for (String extension : Constants.DEFAULT_SCAN_EXTENSIONS) {
            includes.add("**/*." + extension);
        }
        fs.setIncludes(StringUtils.join(includes, ","));

        Path path = new Path(project);
        path.addFileset(fs);
        module.addPath(path);
    }

    private void scanModules() {
        logger.info("Collecting OSS usage information");

        for (Module module : modules) {
            // create project info
            AgentProjectInfo projectInfo = new AgentProjectInfo();
            if (StringUtils.isBlank(module.getName())) {
                projectInfo.setProjectToken(module.getToken());
                logger.info("Processing module with token " + module.getToken());
            } else {
                projectInfo.setCoordinates(new Coordinates(null, module.getName(), null));
                logger.info("Processing " + module.getName());
            }

            // get all files located in module paths
            Set<File> filesToUpdate = new HashSet<File>(); // We're using a set in order to avoid chance of duplicate files.
            for (Path path : module.getPaths()) {
                for (String includedFile : path.list()) {
                    File file = new File(includedFile);
                    if (!file.isDirectory()) {
                        filesToUpdate.add(file);
                    }
                }
            }

            Collection<DependencyInfo> dependencies = projectInfo.getDependencies();
            for (File file : filesToUpdate) {
                dependencies.add(createDependencyInfo(file));
            }

            projectInfos.add(projectInfo);
            logger.info("Found " + dependencies.size() + " direct dependencies");
            debugAgentProjectInfos(projectInfos);
        }
    }

    private DependencyInfo createDependencyInfo(File dependencyFile) {
        String fileName = dependencyFile.getName();
        DependencyInfo dependency = new DependencyInfo();
        try {
            dependency.setFilename(fileName);
            dependency.setArtifactId(fileName);
            dependency.setSystemPath(dependencyFile.getAbsolutePath());

            // Calculate sha1
            dependency.setSha1(ChecksumUtils.calculateSHA1(dependencyFile));

            // Calculate md5
            dependency.addChecksum(ChecksumType.MD5, ChecksumUtils.calculateHash(dependencyFile, HashAlgorithm.MD5));

            // handle JavaScript files
            if (fileName.toLowerCase().matches(JAVA_SCRIPT_REGEX)) {
                Map<ChecksumType, String> javaScriptChecksums;
                try {
                    javaScriptChecksums = new HashCalculator().calculateJavaScriptHashes(dependencyFile);
                    for (Map.Entry<ChecksumType, String> entry : javaScriptChecksums.entrySet()) {
                        dependency.addChecksum(entry.getKey(), entry.getValue());
                    }
                } catch (Exception e) {
                    logger.warn("Failed to calculate javaScript hash for file: " + dependencyFile.getPath() + ", error: "+e.getMessage());
                }
            }

            // Calculate super hash
            ChecksumUtils.calculateSuperHash(dependency, dependencyFile);
        } catch (IOException e) {
            logger.error("Failed to create dependency " + fileName + " to dependency list: " + e.getMessage());
            dependency = null;
        }
        return dependency;
    }

    private void createService() {
        logger.info("Service Url is " + wssUrl);
        service = new WhitesourceService(Constants.AGENT_TYPE, Constants.AGENT_VERSION, Constants.PLUGIN_VERSION, wssUrl);
    }

    private void checkPolicies() {
        if (shouldCheckPolicies) {
            logger.info("Checking policies");
            try {
                CheckPolicyComplianceResult result = service.checkPolicyCompliance(
                        apiKey, product, productVersion, projectInfos, forceCheckAllDependencies);
                handlePoliciesResult(result);
            } catch (WssServiceException e) {
                error(e);
            }
        }
    }

    private void handlePoliciesResult(CheckPolicyComplianceResult result) {
        // generate report
        try {
            logger.info("Creating policies report");
            PolicyCheckReport report = new PolicyCheckReport(result);
            report.generate(policyCheck.getReportdir(), false);
        } catch (IOException e) {
            error(e);
        }

        // handle rejections if any
        if (result.hasRejections()) {
            String rejectionsErrorMessage = "Some dependencies does not conform with open source policies, see report for details.";
            String forceUpdateMessage = "Some dependencies violate open source policies, however all were force updated to organization inventory.";
            if (forceUpdate) {
                if (policyCheck.isFailonrejection()) {
                    logger.warn(forceUpdateMessage);
                    error(rejectionsErrorMessage);
                }
            } else if (policyCheck.isFailonrejection()) {
                logger.warn(rejectionsErrorMessage);
            }
        } else {
            logger.info("All dependencies conform with open source policies");
        }
    }

    private void updateInventory() {
        logger.info("Updating White Source");
        try {
            UpdateInventoryResult result = service.update(apiKey, product, productVersion, projectInfos);
            logUpdateResult(result);
        } catch (WssServiceException e) {
            error("A problem occurred while updating projects: " + e.getMessage());
        }
    }

    private void logUpdateResult(UpdateInventoryResult result) {
        logger.info("White Source update results:");
        logger.info("White Source organization: " + result.getOrganization());

        // newly created projects
        Collection<String> createdProjects = result.getCreatedProjects();
        if (createdProjects.isEmpty()) {
            logger.info("No new projects found");
        } else {
            logger.info(createdProjects.size() + " Newly created projects:");
            for (String projectName : createdProjects) {
                logger.info(projectName);
            }
        }

        // updated projects
        Collection<String> updatedProjects = result.getUpdatedProjects();
        if (updatedProjects.isEmpty()) {
            logger.info("No projects were updated");
        } else {
            logger.info(updatedProjects.size() + " existing projects were updated:");
            for (String projectName : updatedProjects) {
                logger.info(projectName);
            }
        }
    }

    private void error(String errorMsg) {
        if (failOnError) {
            throw new BuildException(errorMsg);
        } else {
            logger.error(errorMsg);
        }
    }

    private void error(Exception ex) {
        if (failOnError) {
            throw new BuildException(ex);
        } else {
            if(ex != null) {
                logger.error(ex.getMessage());
            }
        }
    }

    private void debugAgentProjectInfos(Collection<AgentProjectInfo> projectInfos) {
        logger.debug("----------------- dumping projectInfos -----------------");
        logger.debug("Total number of projects : " + projectInfos.size());
        for (AgentProjectInfo projectInfo : projectInfos) {
            logger.debug("Project coordinates: " + projectInfo.getCoordinates());
            logger.debug("Project parent coordinates: " + projectInfo.getParentCoordinates());
            logger.debug("Project project token: " + projectInfo.getProjectToken());

            Collection<DependencyInfo> dependencies = projectInfo.getDependencies();
            logger.debug("total # of dependencies: " + dependencies.size());
            for (DependencyInfo info : dependencies) {
                logger.debug(info + " SHA-1: [" + info.getSha1() + "], SuperHash: [" + info.getFullHash() + "]");
            }
        }
        logger.debug("----------------- dump finished -----------------");
    }

    /* --- Property set methods --- */

    public void setFailonerror(boolean failonerror) {
        this.failOnError = failonerror;
    }

    public void addModule(Module module) {
        this.modules.add(module);
    }

    public void setApikey(String apikey) {
        this.apiKey = apikey;
    }

    public void addCheckpolicies(CheckPolicies checkpolicies) {
        this.checkPolicies.add(checkpolicies);
    }

    public void setWssurl(String wssurl) {
        this.wssUrl = wssurl;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public void setProductVersion(String productVersion) {
        this.productVersion = productVersion;
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }
}
