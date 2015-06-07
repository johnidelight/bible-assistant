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

package org.heavenus.bible.provider;

import java.util.List;

import android.net.Uri;
import android.provider.BaseColumns;

public class BibleStore {
	public static final Uri BIBLE_CONTENT_URI = BibleProvider.CONTENT_URI;
	public static final Uri BIBLE_MARK_CONTENT_URI = BibleMarkProvider.CONTENT_URI;
	
	public static final String SECTION_NAME_REGEX = "\\d+[a-z]?:-?\\d+x*t*";

	public static final class LocaleColumns implements BaseColumns {
		/* The bible locale name.
		 * Type: TEXT
		 */
		public static final String LOCALE = "locale";
	}

	public static final class CategoryColumns implements BaseColumns {
		/* The category title.
		 * Type: TEXT
		 */
		public static final String TITLE = "title";
	}
	
	public static final class BookColumns implements BaseColumns {
		/* The category id of the book.
		 * Type: INTEGER
		 */
		public static final String CATEGORY_ID = "category_id";

		/* The book name.
		 * Type: TEXT
		 */
		public static final String NAME = "name";
		
		/* The book title.
		 * Type: TEXT
		 */
		public static final String TITLE = "title";

		/* The short book title.
		 * Type: TEXT
		 */
		public static final String TITLE_SHORT = "title_short";
	}
	
	public static final class BookContentColumns implements BaseColumns {
		/* The section name.
		 * Type: TEXT
		 */
		public static final String SECTION = "section";
		
		/* The content of the section.
		 * Type: TEXT
		 */
		public static final String CONTENT = "content";
	}

	public static final class BookMarkColumns implements BaseColumns {
		/* The book name.
		 * Type: TEXT
		 */
		public static final String BOOK_NAME = "book_name";
		
		/* The current reading section id of the book.
		 * Type: INTEGER
		 */
		public static final String SECTION_ID = "section_id";
		
		/* The current reading section name of the book.
		 * Type: TEXT
		 */
		public static final String SECTION_NAME = "section_name";
	}

	public static final class BookCommentColumns implements BaseColumns {
		/* The section name.
		 * Type: TEXT
		 */
		public static final String SECTION = "section";
		
		/* The comment of the section.
		 * Type: TEXT
		 */
		public static final String COMMENT = "comment";
	}

	public static String getBookName(Uri uri) {
		if(uri == null) return null;
		
		String bookName = null;
		
		String authority = uri.getAuthority();
		if(BibleProvider.AUTHORITY.equals(authority)) {
			List<String> segs = uri.getPathSegments();
			if(segs.size() > BibleProvider.URI_SEGMENT_INDEX_BOOK) {
				bookName = segs.get(BibleProvider.URI_SEGMENT_INDEX_BOOK);
			}
		} else if(BibleMarkProvider.AUTHORITY.equals(authority)) {
			List<String> segs = uri.getPathSegments();
			if(segs.size() > BibleMarkProvider.URI_SEGMENT_INDEX_BOOK_NAME) {
				bookName = segs.get(BibleMarkProvider.URI_SEGMENT_INDEX_BOOK_NAME);
			}
		}
		
		return bookName;
	}
	
	public static String getSectionName(Uri uri) {
		if(uri == null) return null;
		
		String sectionName = null;
		
		String authority = uri.getAuthority();
		if(BibleProvider.AUTHORITY.equals(authority)) {
			List<String> segs = uri.getPathSegments();
			if(segs.size() > BibleProvider.URI_SEGMENT_INDEX_SECTION) {
				sectionName = segs.get(BibleProvider.URI_SEGMENT_INDEX_SECTION);
			}
		} else if(BibleMarkProvider.AUTHORITY.equals(authority)) {
			List<String> segs = uri.getPathSegments();
			if(segs.size() > BibleMarkProvider.URI_SEGMENT_INDEX_SECTION_NAME) {
				sectionName = segs.get(BibleMarkProvider.URI_SEGMENT_INDEX_SECTION_NAME);
			}
		}
		
		return sectionName;
	}
}