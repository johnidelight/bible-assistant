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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.text.TextUtils;

/*
 * Book content format:
 * <section_name><TAB><section_content>
 * <section_name><TAB><section_content>
 * ...
 */
class Book {
	public class Section {
		public String name;
		public String content;
	}
	
	private String mName;
	private ArrayList<Section> mSections = new ArrayList<Section>();
	

	public Book(File f) throws IOException {
		mName = Path.getFileNameWithoutExtension(f.getAbsolutePath());
		init(f);
	}
	
	public String getName() {
		return mName;
	}

	public int getSectionCount() {
		return mSections.size();
	}
	
	public Section getSection(int index) {
		if(index >= 0 && index < mSections.size()) {
			return mSections.get(index);
		}
		
		return null;
	}
	
	private void init(File book) throws IOException {
		InputStream in = null;
		try {
			// Read all book content.
			in = new BufferedInputStream(new FileInputStream(book));
			byte[] data = new byte[in.available()];
			in.read(data);
			
			// Skip beginning encoding data.
			String content = new String(data).substring(1);

			// Parse every line.
			String[] rows = content.split("\r\n");
			for(String r : rows) {
				// Parse every field.
				if(!TextUtils.isEmpty(r)) {
					String[] fields = r.split("\t");
					int count = fields.length;
					if(count > 0) {
						// Add new section.
						Section s = new Section();
						s.name = fields[0].trim();
						s.content = (count > 1 ? fields[1] : "");
						mSections.add(s);
					}
				}
			}
		} finally {
		   if (in != null) {
			   in.close();
			}
		}
	}
}