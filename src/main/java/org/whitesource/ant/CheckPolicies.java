package org.whitesource.ant;

import java.io.File;

/**
 * Nested element in the White Source ant task, which checks for policies and generates a report.
 * 
 * @author tom.shapira
 *
 */
public class CheckPolicies {
	
	/* --- Members --- */
	
	/**
	 * Output directory for White Source generated report file.
	 */
	private File reportdir;
	
	/**
	 * Whether or not to fail the build if policy rejects a library.
	 */
	private boolean failonrejection;
	
	/* --- Constructors --- */
	
	public CheckPolicies() {
		failonrejection = true;
	}

	/* --- Getters / Setters --- */
	
	public boolean isFailonrejection() {
		return failonrejection;
	}
	
	public void setFailonrejection(boolean failonrejection) {
		this.failonrejection = failonrejection;
	}
	
	public void setReportdir(File reportdir) {
		this.reportdir = reportdir;
	}

	public File getReportdir() {
		return reportdir;
	}
}
