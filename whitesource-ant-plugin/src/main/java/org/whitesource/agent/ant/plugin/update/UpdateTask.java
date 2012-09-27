package org.whitesource.agent.ant.plugin.update;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.sonatype.aether.util.ChecksumUtils;
import org.whitesource.agent.ant.plugin.Constants;

/**
 * Sends an update request to White Source.
 * 
 * @author tom.shapira
 *
 */
public class UpdateTask extends Task {

	/* --- Property members --- */

	/**
	 * Project name to be matched with White Source project name.
	 */
	private String projectname;

	/**
	 * Project token to match White Source project.
	 */
	private String projecttoken;
	
	/**
	 * Paths to folders and files to update.
	 */
	private Vector<Path> paths;
	
	/**
	 * Whether or not to stop the build when encountering an error.
	 */
	private boolean stopbuild;
	
	/* --- Members --- */
	
	private Set<File> fileToUpdate;

	/* --- Overridden Ant Task methods --- */

	@Override
	public void init() throws BuildException {
		super.init();

		paths = new Vector<Path>();
		stopbuild = true;
		fileToUpdate = new HashSet<File>();
	}

	@Override
	public void execute() throws BuildException {
		validate();

		// get all included files
		for (Path path : paths) {
			for (String includedFile : path.list()) {
				fileToUpdate.add(new File(includedFile));
			}
		}
		
		// calculate SHA-1 for all files
		for (File file : fileToUpdate) {
			String sha1 = calculateSha1(file);
			log("SHA-1 for '" + file.getName() + "' = " + sha1);
		}
		
		// TODO send update
	}

	/* --- Protected methods --- */

	protected void validate() {
		if (paths.size() < 1) {
			error(Constants.ERROR_PATH);
		}
		
		if (projectname == null && projecttoken == null) {
			error(Constants.ERROR_PROJECT_IDENTIFIER);
		}
	}

	protected void error(String errorMsg) {
		if (stopbuild) {
			throw new BuildException(errorMsg);
		} else {
			log(errorMsg);
		}
	}
	
	/* --- Private methods --- */
	
	/**
	 * Calculates SHA-1 for the specified file.
	 * 
	 * @param file File.
	 * 
	 * @return SHA-1 calculation.
	 */
	private String calculateSha1(File file) {
		String sha1 = null;
		if (file != null) {
			try {
				Map<String, Object> calcMap = ChecksumUtils.calc(file, Arrays.asList(Constants.SHA1));
				sha1 = (String) calcMap.get(Constants.SHA1);
			} catch (IOException e) {
				error(Constants.ERROR_SHA1 + " for file '" + file.getName() + "'");
			}
		}
		return sha1;
	}
	
	/* --- Property set methods --- */

	public void setProjectname(String projectname) {
		this.projectname = projectname;
	}

	public void setStopbuild(boolean stopbuild) {
		this.stopbuild = stopbuild;
	}
	
	public void addPath(Path path) {
		paths.add(path);
	}

	public void setProjecttoken(String projecttoken) {
		this.projecttoken = projecttoken;
	}

}
