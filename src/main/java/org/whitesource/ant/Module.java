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

import org.apache.tools.ant.types.Path;

import java.util.Vector;

/**
 * This object models a project module in the workspace.
 * Usage - Declare a <module/> tag in the ant task.
 * 
 * @author tom.shapira
 *
 */
public class Module {
	
	/**
	 * Module name to be matched with White Source project name.
	 */
	private String name;
	
	/**
	 * Module token to match White Source project.
	 */
	private String token;
	
	/**
	 * Paths to folders and files to update.
	 */
	private Vector<Path> paths;
	
	/* --- Constructors --- */
	
	public Module() {
		paths = new Vector<Path>();
	}
	
	/* --- Getters / Setters --- */
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Vector<Path> getPaths() {
		return paths;
	}

	public void addPath(Path path) {
		this.paths.add(path);
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
