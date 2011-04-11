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
	public static final Uri BIBLE_COMMENT_CONTENT_URI = BibleCommentProvider.CONTENT_URI;

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
		/* The book name.
		 * Type: TEXT
		 */
		public static final String NAME = "name";
		
		/* The book title.
		 * Type: TEXT
		 */
		public static final String TITLE = "title";
		
		/* The category id of the book.
		 * Type: INTEGER
		 */
		public static final String CATEGORY_ID = "category_id";
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
		} else if(BibleCommentProvider.AUTHORITY.equals(authority)) {
			List<String> segs = uri.getPathSegments();
			if(segs.size() > BibleCommentProvider.URI_SEGMENT_INDEX_BOOK) {
				bookName = segs.get(BibleCommentProvider.URI_SEGMENT_INDEX_BOOK);
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
			if(segs.size() > BibleProvider.URI_SEGMENT_INDEX_BOOK) {
				sectionName = segs.get(BibleProvider.URI_SEGMENT_INDEX_SECTION);
			}
		} else if(BibleCommentProvider.AUTHORITY.equals(authority)) {
			List<String> segs = uri.getPathSegments();
			if(segs.size() > BibleCommentProvider.URI_SEGMENT_INDEX_BOOK) {
				sectionName = segs.get(BibleCommentProvider.URI_SEGMENT_INDEX_SECTION);
			}
		}
		
		return sectionName;
	}
}