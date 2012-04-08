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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/* Bible content provider
 * 
 *	Content Uri format:
 *		content://<authority>/<locale_name>/<category_id>/<book_name>/<section_name>
 *		For example:
 *			content://org.heavenus.bible/en_US/1/book_god/1.1
 *			content://org.heavenus.bible/zh_CN/2/book_jesus/2.11
 * 
 *	Section name format:
 *		chapter_number[appendix_letter].section_number[t]
 *		(1) chapter_number:  >0
 *		(2) appendix_letter: [a-z]
 *		(3) dot
 *		(4) section_number: positive or negative
 *		(5) t: title if present; otherwise body

 *		Examples:
 *		(1) Main title section: 1.-1t
 *		(2) Chapter title section: 1.0t
 *		(3) Part title section: 1.1t
 *		(4) Main body section: 1.1
 *		(5) Appendix title section: 1a.0t
 *		(6) Appendix body section: 1a.1
 */
public class BibleProvider extends ContentProvider {
	static final String AUTHORITY = "org.heavenus.bible";
	static final String MIMETYPE_BASE = "vnd.heavenus.bible";
	static final Uri CONTENT_URI = Uri.parse(new StringBuilder(
			ContentResolver.SCHEME_CONTENT).append("://").append(AUTHORITY).toString());

	static final int URI_SEGMENT_INDEX_LOCALE = 0;
	static final int URI_SEGMENT_INDEX_CATEGORY = 1;
	static final int URI_SEGMENT_INDEX_BOOK = 2;
	static final int URI_SEGMENT_INDEX_SECTION = 3;

	private static final int MATCH_ROOT = 1;
	private static final int MATCH_LOCALE = 2;
	private static final int MATCH_CATEGORY = 3;
	private static final int MATCH_BOOK = 4;
	private static final int MATCH_SECTION = 5;

	private static final UriMatcher URI_MATCHER = new UriMatcher(MATCH_ROOT);
	static {
		URI_MATCHER.addURI(AUTHORITY, "*", MATCH_LOCALE);
		URI_MATCHER.addURI(AUTHORITY, "*/#", MATCH_CATEGORY);
		URI_MATCHER.addURI(AUTHORITY, "*/#/*", MATCH_BOOK);
		URI_MATCHER.addURI(AUTHORITY, "*/#/*/*", MATCH_SECTION);
	}

	private static final String DATABASE_PATH = "/lib/libbible_*.so";
	private static final String TABLE_CATEGORY = "categories";
	private static final String TABLE_BOOK = "books";

	private Map<String, String> mDatabases; // <locale_name, database_path>

	@Override
	public String getType(Uri uri) {
		String mimeType = null;

		switch(URI_MATCHER.match(uri)) {
		case MATCH_ROOT:
			mimeType = new StringBuilder(ContentResolver.CURSOR_DIR_BASE_TYPE)
					.append('/').append(MIMETYPE_BASE).append(".locale").toString();
			break;
		case MATCH_LOCALE:
			mimeType = new StringBuilder(ContentResolver.CURSOR_DIR_BASE_TYPE)
					.append('/').append(MIMETYPE_BASE).append(".category").toString();
			break;
		case MATCH_CATEGORY:
			mimeType = new StringBuilder(ContentResolver.CURSOR_DIR_BASE_TYPE)
					.append('/').append(MIMETYPE_BASE).append(".book").toString();
			break;
		case MATCH_BOOK:
			mimeType = new StringBuilder(ContentResolver.CURSOR_DIR_BASE_TYPE)
					.append('/').append(MIMETYPE_BASE).append(".section").toString();
			break;
		case MATCH_SECTION:
			mimeType = new StringBuilder(ContentResolver.CURSOR_ITEM_BASE_TYPE)
					.append('/').append(MIMETYPE_BASE).append(".section").toString();
			break;
		default:
			break;
		}

		return mimeType;
	}

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int matchCode = URI_MATCHER.match(uri);
        if(matchCode == MATCH_ROOT) {
        	// Get all available locales.
        	MatrixCursor c = new MatrixCursor(new String[]{BibleStore.LocaleColumns._ID, BibleStore.LocaleColumns.LOCALE});
        	Set<String> locales = getDatabases(getContext()).keySet();
        	int id = 1;
        	for(String locale : locales) {
        		c.newRow().add(id ++).add(locale);
        	}
        	
        	return c;
        }
		
		// Get corresponding database.
		SQLiteDatabase db = getDatabaseForUri(uri);
        if (db == null) {
            return null;
        }

        // Build sql script.
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		switch(matchCode) {
		case MATCH_LOCALE:
			builder.setTables(TABLE_CATEGORY);
			break;
		case MATCH_CATEGORY:
			{
				String categoryId = uri.getPathSegments().get(URI_SEGMENT_INDEX_CATEGORY);
				builder.setTables(TABLE_BOOK);
				builder.appendWhere(new StringBuilder(
						BibleStore.BookColumns.CATEGORY_ID).append('=').append(categoryId).toString());
			}
			break;
		case MATCH_BOOK:
			{
				String bookName = uri.getPathSegments().get(URI_SEGMENT_INDEX_BOOK);
				builder.setTables(bookName); // Book name is the table name.
			}
			break;
		case MATCH_SECTION:
			{
				String bookName = uri.getPathSegments().get(URI_SEGMENT_INDEX_BOOK);
				String sectionName = uri.getPathSegments().get(URI_SEGMENT_INDEX_SECTION);
				builder.setTables(bookName); // Book name is the table name.
				builder.appendWhere(new StringBuilder(
						BibleStore.BookContentColumns.SECTION).append("=\'").append(sectionName).append('\'').toString());
			}
			break;
		default:
			break;
		}
		
		// Query database finally.
		Cursor c = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder, null);
        if (c != null) {
            c.setNotificationUri(getContext().getContentResolver(), uri);
        }

		return c;
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}
	
	private synchronized Map<String, String> getDatabases(Context c) {
		if(mDatabases == null) {
			mDatabases = new HashMap<String, String>();
			
			// Find all available locale databases.
			String pathPattern = new StringBuilder(c.getFilesDir().getParent()).append(DATABASE_PATH).toString();
			Locale[] locales = Locale.getAvailableLocales();
			for(Locale locale : locales) {
				String localeName = locale.toString();
				String path = pathPattern.replace("*", localeName);
				File f = new File(path);
				if(f.exists()) {
					mDatabases.put(localeName, path);
				}
			}
		}
		
		return mDatabases;
	}

	private SQLiteDatabase getDatabaseForUri(Uri uri) {
		if(uri == null) return null;

		// Get corresponding database path.
		String path = null;
		List<String> segments = uri.getPathSegments();
		if(segments.size() > URI_SEGMENT_INDEX_LOCALE) {
			String locale = segments.get(URI_SEGMENT_INDEX_LOCALE);

			Map<String, String> dbs = getDatabases(getContext());
			if(dbs.containsKey(locale)) {
				path = dbs.get(locale);
			}
		}
		
		// Open database finally.
		SQLiteDatabase db = null;
		if(path != null) {
			try {
				db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
			} catch(SQLiteException e) {
				e.printStackTrace();
			}
		}

		return db;
	}
}