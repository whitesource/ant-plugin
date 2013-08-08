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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public static final List<String> DEFAULT_SCAN_EXTENSIONS = new ArrayList<String>();
    static {
        DEFAULT_SCAN_EXTENSIONS.addAll(
                Arrays.asList("jar", "war", "ear", "par", "rar",
                        "dll", "exe", "ko", "so", "msi",
                        "zip", "tar", "tar.gz",
                        "swc", "swf"));
    }

	/* --- Constructors --- */
	
	/**
	 * Private default constructor
	 */
	private Constants() {
		// avoid instantiation
	}

}