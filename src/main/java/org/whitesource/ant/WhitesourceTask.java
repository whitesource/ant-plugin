/**
 * Copyright (C) 2012 White Source Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.whitesource.ant;

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
import org.whitesource.agent.report.PolicyCheckReport;
import org.whitesource.agent.client.WhitesourceService;
import org.whitesource.agent.client.WssServiceException;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Sends an inventory update request to White Source.
 *
 * @author tom.shapira
 */
public class WhitesourceTask extends Task {

	/* --- Property members --- */

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

	/* --- Members --- */

    private boolean shouldCheckPolicies;

    private CheckPolicies policyCheck;

    private Collection<AgentProjectInfo> projectInfos;

    private WhitesourceService service;

	/* --- Overridden Ant Task methods --- */

    @Override
    public void init() throws BuildException {
        super.init();

        modules = new Vector<Module>();
        checkPolicies = new Vector<CheckPolicies>();
        failOnError = true;
        shouldCheckPolicies = false;
        projectInfos = new ArrayList<AgentProjectInfo>();
    }

    @Override
    public void execute() throws BuildException {
        validate();
        scanModules();
        createService();
        checkPolicies();
        updateInventory();
    }

	/* --- Private methods --- */

    private void validate() {
        // api key
        if (StringUtils.isBlank(apiKey)) {
            error("Missing API Key");
        }

        // modules
        if (modules.isEmpty()) {
            Module module = new Module(); // Treat whole project as single module
            String name = getProject().getName();
            module.setName(StringUtils.isBlank(name) ? "Default Module" : name);
            addDefaultPaths(module);
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
            File reportDir = policyCheck.getReportdir();
            if (reportDir == null) {
                reportDir = new File(this.getProject().getBaseDir(), "whitesource");
                policyCheck.setReportdir(reportDir);
            }
            if (!reportDir.exists() && !reportDir.mkdirs()) {
                error("Policies report directory doesn't exists and can not be created");
            }
        }
    }

    private void addDefaultPaths(Module module) {
        Project project = getProject();
        for (String extension : Constants.DEFAULT_SCAN_EXTENSIONS) {
            module.addPath(new Path(project, "**/*." + extension));
        }
    }

    private void scanModules() {
        log("Collecting OSS usage information");

        for (Module module : modules) {
            // create project info
            AgentProjectInfo projectInfo = new AgentProjectInfo();
            if (StringUtils.isBlank(module.getName())) {
                projectInfo.setProjectToken(module.getToken());
                log("Processing module with token " + module.getToken());
            } else {
                projectInfo.setCoordinates(new Coordinates(null, module.getName(), null));
                log("Processing " + module.getName());
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

            // calculate SHA-1 for all files
            Collection<DependencyInfo> dependencies = projectInfo.getDependencies();
            for (File file : filesToUpdate) {
                String fileName = file.getName();
                DependencyInfo dependency = new DependencyInfo();
                dependency.setArtifactId(fileName);
                dependency.setSystemPath(file.getAbsolutePath());
                try {
                    dependency.setSha1(ChecksumUtils.calculateSHA1(file));
                } catch (IOException e) {
                    log("Problem calculating SHA-1 for '" + fileName + "'", e, Project.MSG_DEBUG);
                }
                dependencies.add(dependency);
            }

            projectInfos.add(projectInfo);
            log("Found " + dependencies.size() + " direct dependencies");
            debugAgentProjectInfos(projectInfos);
        }
    }

    private void createService() {
        log("Service Url is " + wssUrl, Project.MSG_DEBUG);
        service = new WhitesourceService(Constants.AGENT_TYPE, Constants.AGENT_VERSION, wssUrl);
    }

    private void checkPolicies() {
        if (shouldCheckPolicies) {
            log("Checking policies");
            try {
                CheckPoliciesResult result = service.checkPolicies(apiKey, product, productVersion, projectInfos);
                handlePoliciesResult(result);
            } catch (WssServiceException e) {
                error(e);
            }
        }
    }

    private void handlePoliciesResult(CheckPoliciesResult result) {
        // generate report
        try {
            log("Creating policies report", Project.MSG_INFO);
            PolicyCheckReport report = new PolicyCheckReport(result);
            report.generate(policyCheck.getReportdir(), false);
        } catch (IOException e) {
            error(e);
        }

        // handle rejections if any
        if (result.hasRejections()) {
            String rejectionsErrorMessage = "Some dependencies does not conform with open source policies, see report for details.";
            if (policyCheck.isFailonrejection()) {
                throw new BuildException(rejectionsErrorMessage);
            } else {
                log(rejectionsErrorMessage, Project.MSG_WARN);
            }
        } else {
            log("All dependencies conform with open source policies");
        }
    }

    private void updateInventory() {
        log("Updating White Source");
        try {
            UpdateInventoryResult result = service.update(apiKey, product, productVersion, projectInfos);
            logUpdateResult(result);
        } catch (WssServiceException e) {
            error("A problem occurred while updating projects: " + e.getMessage());
        }
    }

    private void logUpdateResult(UpdateInventoryResult result) {
        log("White Source update results:");
        log("White Source organization: " + result.getOrganization());

        // newly created projects
        Collection<String> createdProjects = result.getCreatedProjects();
        if (createdProjects.isEmpty()) {
            log("No new projects found");
        } else {
            log(createdProjects.size() + " Newly created projects:");
            for (String projectName : createdProjects) {
                log(projectName);
            }
        }

        // updated projects
        Collection<String> updatedProjects = result.getUpdatedProjects();
        if (updatedProjects.isEmpty()) {
            log("No projects were updated");
        } else {
            log(updatedProjects.size() + " existing projects were updated:");
            for (String projectName : updatedProjects) {
                log(projectName);
            }
        }
    }

    private void error(String errorMsg) {
        if (failOnError) {
            throw new BuildException(errorMsg);
        } else {
            log(errorMsg, Project.MSG_ERR);
        }
    }

    private void error(Exception ex) {
        if (failOnError) {
            throw new BuildException(ex);
        } else {
            log(ex, Project.MSG_ERR);
        }
    }

    private void debugAgentProjectInfos(Collection<AgentProjectInfo> projectInfos) {
        log("----------------- dumping projectInfos -----------------", Project.MSG_DEBUG);
        log("Total number of projects : " + projectInfos.size(), Project.MSG_DEBUG);
        for (AgentProjectInfo projectInfo : projectInfos) {
            log("Project coordinates: " + projectInfo.getCoordinates(), Project.MSG_DEBUG);
            log("Project parent coordinates: " + projectInfo.getParentCoordinates(), Project.MSG_DEBUG);
            log("Project project token: " + projectInfo.getProjectToken(), Project.MSG_DEBUG);

            Collection<DependencyInfo> dependencies = projectInfo.getDependencies();
            log("total # of dependencies: " + dependencies.size(), Project.MSG_DEBUG);
            for (DependencyInfo info : dependencies) {
                log(info + " SHA-1: " + info.getSha1(), Project.MSG_DEBUG);
            }
        }
        log("----------------- dump finished -----------------", Project.MSG_DEBUG);
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

    public void setProduct(String product) {
        this.product = product;
    }

    public void setProductVersion(String productVersion) {
        this.productVersion = productVersion;
    }
}
