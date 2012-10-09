package org.whitesource.ant;

import java.util.Vector;

import org.apache.tools.ant.types.Path;

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
