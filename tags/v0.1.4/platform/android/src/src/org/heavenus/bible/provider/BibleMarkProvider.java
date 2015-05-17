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

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

/* Bible mark content provider
 * 
 *	Content Uri format:
 *		content://<authority>/<book_name>/<section_name>
 *		For example:
 *			content://org.heavenus.bible.mark/book_god
 *			content://org.heavenus.bible.mark/book_god/1.1
 *			content://org.heavenus.bible.mark/book_jesus/2.11
 *
 *	Section name format: same with definition in Bible content provider.
 */
public class BibleMarkProvider extends ContentProvider {
	static final String AUTHORITY = "org.heavenus.bible.mark";
	static final String MIMETYPE_BASE = "vnd.heavenus.bible.mark";
	static final Uri CONTENT_URI = Uri.parse(new StringBuilder(
			ContentResolver.SCHEME_CONTENT).append("://").append(AUTHORITY).toString());

	static final int URI_SEGMENT_INDEX_BOOK = 0;
	static final int URI_SEGMENT_INDEX_SECTION = 1;

	private static final int MATCH_BOOK = 1;
	private static final int MATCH_SECTION = 2;

	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		URI_MATCHER.addURI(AUTHORITY, "*", MATCH_BOOK);
		URI_MATCHER.addURI(AUTHORITY, "*/*", MATCH_SECTION);
	}
	
	private static final String DATABASE_NAME = "bible_mark.db";
	private static final String TABLE_BOOKMARK = "bookmark";

	private DatabaseHelper mDbHelper;

	@Override
	public String getType(Uri uri) {
		String mimeType = null;

		switch(URI_MATCHER.match(uri)) {
		case MATCH_BOOK:
			mimeType = new StringBuilder(ContentResolver.CURSOR_ITEM_BASE_TYPE)
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
		mDbHelper = new DatabaseHelper(getContext(), DATABASE_NAME);
		
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		// Get current database.
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		
        // Build sql script.
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		switch(URI_MATCHER.match(uri)) {
		case MATCH_BOOK:
			{
				String bookName = uri.getPathSegments().get(URI_SEGMENT_INDEX_BOOK);
				builder.setTables(TABLE_BOOKMARK);
				builder.appendWhere(new StringBuilder(
						BibleStore.BookMarkColumns.NAME).append("=\'").append(bookName).append('\'').toString());

				synchronized(mDbHelper) {
					ensureBookMarkTable(db, TABLE_BOOKMARK);
				}
			}
			break;
		case MATCH_SECTION:
			{
				String bookName = uri.getPathSegments().get(URI_SEGMENT_INDEX_BOOK);
				String section = uri.getPathSegments().get(URI_SEGMENT_INDEX_SECTION);
				builder.setTables(bookName); // Book name is the table name.
				builder.appendWhere(new StringBuilder(
						BibleStore.BookCommentColumns.SECTION).append("=\'").append(section).append('\'').toString());

				synchronized(mDbHelper) {
					ensureBookCommentTable(db, bookName);
				}
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
		// Get current database.
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		
        // Build sql script.
        String table = null;
        String where = null;
		switch(URI_MATCHER.match(uri)) {
		case MATCH_BOOK:
			{
				table = TABLE_BOOKMARK;
				String bookName = uri.getPathSegments().get(URI_SEGMENT_INDEX_BOOK);
				StringBuilder sb = new StringBuilder(BibleStore.BookMarkColumns.NAME)
						.append("=\'").append(bookName).append('\'');
				if(!TextUtils.isEmpty(selection)) {
					sb.append(" AND (").append(selection).append(')');
				}
				where = sb.toString();
				
				synchronized(mDbHelper) {
					ensureBookMarkTable(db, table);
				}
			}
			break;
		case MATCH_SECTION:
			{
				table = uri.getPathSegments().get(URI_SEGMENT_INDEX_BOOK); // Book name is the table name.
				String section = uri.getPathSegments().get(URI_SEGMENT_INDEX_SECTION);
				StringBuilder sb = new StringBuilder(BibleStore.BookCommentColumns.SECTION)
						.append("=\'").append(section).append('\'');
				if(!TextUtils.isEmpty(selection)) {
					sb.append(" AND (").append(selection).append(')');
				}
				where = sb.toString();
				
				synchronized(mDbHelper) {
					ensureBookCommentTable(db, table);
				}
			}
			break;
		default:
			break;
		}
		
		// Update database finally.
		int count = 0;
		if(table != null) {
			synchronized(mDbHelper) {
				count = db.update(table, values, where, selectionArgs);
			}
			if (count > 0) {
	            getContext().getContentResolver().notifyChange(uri, null);
	        }
		}

        return count;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Uri newUri = null;
		switch(URI_MATCHER.match(uri)) {
		case MATCH_BOOK:
			{
				String bookName = uri.getPathSegments().get(URI_SEGMENT_INDEX_BOOK);
				String section = values.getAsString(BibleStore.BookMarkColumns.SECTION);
				if(section == null) section = "";
				
				// Check whether current book mark already exist.
				boolean bookMarkExist = false;
				StringBuilder where = new StringBuilder(BibleStore.BookMarkColumns.NAME)
						.append("=\'").append(bookName).append('\'');
				Cursor c = query(uri, null, where.toString(), null, null);
				if(c != null) {
					bookMarkExist = (c.getCount() > 0);
					c.close();
				}
				
				// Ensure current book mark uniquely.
				if(bookMarkExist) {
					if(update(uri, values, null, null) > 0) {
						newUri = uri;
					}
				} else {
					// Insert new book mark finally.
					SQLiteDatabase db = mDbHelper.getWritableDatabase();
					long rowId = -1;
					synchronized(mDbHelper) {
						ensureBookMarkTable(db, TABLE_BOOKMARK);
						rowId = db.insert(TABLE_BOOKMARK, null, values);
					}

					if(rowId != -1) {
						newUri = uri;
					}
				}
			}
			break;
		case MATCH_SECTION:
			{
				// Ensure valid section name.
				String section = values.getAsString(BibleStore.BookCommentColumns.SECTION);
				if(!TextUtils.isEmpty(section)) {
					// Check whether current section already exist.
					String bookName = uri.getPathSegments().get(URI_SEGMENT_INDEX_BOOK);
					Uri bookUri = Uri.withAppendedPath(CONTENT_URI, bookName);
					Uri sectionUri = Uri.withAppendedPath(bookUri, section);
					boolean sectionExist = false;
					Cursor c = query(sectionUri, null, null, null, null);
					if(c != null) {
						sectionExist = (c.getCount() > 0);
						c.close();
					}
					
					// Ensure current section name uniquely.
					if(sectionExist) {
						if(update(sectionUri, values, null, null) > 0) {
							newUri = sectionUri;
						}
					} else {
						// Insert new section finally.
						SQLiteDatabase db = mDbHelper.getWritableDatabase();
						long rowId = -1;
						synchronized(mDbHelper) {
							// Book name is the table name.
							ensureBookCommentTable(db, bookName);
							rowId = db.insert(bookName, null, values);
						}

						if(rowId != -1) {
							newUri = sectionUri;
						}
					}
				}
			}
			break;
		default:
			break;
		}

        return newUri;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// Get current database.
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		
        // Build sql script.
        String table = null;
        String where = null;
		switch(URI_MATCHER.match(uri)) {
		case MATCH_BOOK:
			{
				table = TABLE_BOOKMARK;
				String bookName = uri.getPathSegments().get(URI_SEGMENT_INDEX_BOOK);
				StringBuilder sb = new StringBuilder(BibleStore.BookMarkColumns.NAME)
						.append("=\'").append(bookName).append('\'');
				if(!TextUtils.isEmpty(selection)) {
					sb.append(" AND (").append(selection).append(')');
				}
				where = sb.toString();
				
				synchronized(mDbHelper) {
					ensureBookMarkTable(db, table);
				}
			}
			break;
		case MATCH_SECTION:
			{
				table = uri.getPathSegments().get(URI_SEGMENT_INDEX_BOOK); // Book name is the table name.
				String section = uri.getPathSegments().get(URI_SEGMENT_INDEX_SECTION);
				StringBuilder sb = new StringBuilder(BibleStore.BookCommentColumns.SECTION)
						.append("=\'").append(section).append('\'');
				if(!TextUtils.isEmpty(selection)) {
					sb.append(" AND (").append(selection).append(')');
				}
				where = sb.toString();
				
				synchronized(mDbHelper) {
					ensureBookCommentTable(db, table);
				}
			}
			break;
		default:
			break;
		}
		
		// Update database finally.
		int count = 0;
		if(table != null) {
			synchronized(mDbHelper) {
				count = db.delete(table, where, selectionArgs);
			}
			if (count > 0) {
	            getContext().getContentResolver().notifyChange(uri, null);
	        }
		}

        return count;
	}

	private void ensureBookMarkTable(SQLiteDatabase db, String table) {
		if(db == null || TextUtils.isEmpty(table)) return;

		db.execSQL(new StringBuilder("CREATE TABLE IF NOT EXISTS ").append(table).append(" (")
				.append(BibleStore.BookMarkColumns._ID).append(" INTEGER PRIMARY KEY,")
				.append(BibleStore.BookMarkColumns.NAME).append(" TEXT,")
				.append(BibleStore.BookMarkColumns.SECTION).append(" TEXT")
				.append(");").toString());
	}

	private void ensureBookCommentTable(SQLiteDatabase db, String table) {
		if(db == null || TextUtils.isEmpty(table)) return;

		db.execSQL(new StringBuilder("CREATE TABLE IF NOT EXISTS ").append(table).append(" (")
				.append(BibleStore.BookCommentColumns._ID).append(" INTEGER PRIMARY KEY,")
				.append(BibleStore.BookCommentColumns.SECTION).append(" TEXT,")
				.append(BibleStore.BookCommentColumns.COMMENT).append(" TEXT")
				.append(");").toString());
	}

	private class DatabaseHelper extends SQLiteOpenHelper {
		private static final int DATABASE_VERSION = 1;
		
		public DatabaseHelper(Context context, String name) {
			super(context, name, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			updateDatabase(db, 0, DATABASE_VERSION);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			updateDatabase(db, oldVersion, newVersion);
		}
		
		private void updateDatabase(SQLiteDatabase db, int fromVersion, int toVersion) {
	        // For future update purpose...
		}
	}
}