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

	/**
	 *
	 */
	private boolean forcecheckalldependencies;

	/* --- Constructors --- */
	
	public CheckPolicies() {
		failonrejection = true;
		forcecheckalldependencies = false;
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

	public boolean isForcecheckalldependencies() {
		return forcecheckalldependencies;
	}

	public void setForcecheckalldependencies(boolean forcecheckalldependencies) {
		this.forcecheckalldependencies = forcecheckalldependencies;
	}
}
