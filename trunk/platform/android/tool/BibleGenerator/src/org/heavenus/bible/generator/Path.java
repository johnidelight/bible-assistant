/*
 * Copyright (C) 2011 The Bible Assistant Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.heavenus.bible.generator;

class Path {
	public static String getFileName(String path) {
		int index = path.lastIndexOf('/');
		if(index != -1)
			return path.substring(index + 1);
		else
			return path;
	}
	
	public static String getFileNameWithoutExtension(String path) {
		String name = getFileName(path);
		int index = name.lastIndexOf('.');
		if(index != -1)
			return name.substring(0, index);
		else
			return name;
	}
	
	public static String getFileExtension(String path) {
		int index = path.lastIndexOf('.');
		if(index != -1)
			return path.substring(index + 1);
		else
			return "";
	}
	
	public static String getFolderPath(String path) {
		int index = path.lastIndexOf('/');
		if(index != -1)
			return path.substring(0, index + 1); // with ending '/'
		else
			return "";
	}
}