package org.heavenus.bible.provider;

import android.provider.BaseColumns;

class BibleStore {
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
}