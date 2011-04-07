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

/* Bible comment provider
 * 
 * Content Uri format:
 *		content://<authority>/<book_name>/<section_name>
 *	For example:
 *		content://org.heavenus.bible.comment/book_god/1.1
 *		content://org.heavenus.bible.comment/book_jesus/2.11
 */
public class BibleCommentProvider extends ContentProvider {
	static final String AUTHORITY = "org.heavenus.bible.comment";
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
	
	private static final String DATABASE_NAME = "bible_comment.db";
	private DatabaseHelper mDbHelper;

	@Override
	public String getType(Uri uri) {
		String mimeType = null;

		switch(URI_MATCHER.match(uri)) {
		case MATCH_BOOK:
			mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/section";
			break;
		case MATCH_SECTION:
			mimeType = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/section";
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
				builder.setTables(bookName); // Book name is the table name.

				// FIXME: it is not necessary to ensure book table exist here.
				synchronized(mDbHelper) {
					ensureBookTable(db, bookName);
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
				
				// FIXME: it is not necessary to ensure book table exist here.
				synchronized(mDbHelper) {
					ensureBookTable(db, bookName);
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
				table = uri.getPathSegments().get(URI_SEGMENT_INDEX_BOOK); // Book name is the table name.
				where = selection;
			}
			break;
		case MATCH_SECTION:
			{
				table  = uri.getPathSegments().get(URI_SEGMENT_INDEX_BOOK); // Book name is the table name.
				String section = uri.getPathSegments().get(URI_SEGMENT_INDEX_SECTION);
				where = new StringBuilder(
						BibleStore.BookCommentColumns.SECTION).append('=').append(section)
						.append("AND (").append(selection).append(')').toString();
			}
			break;
		default:
			break;
		}
		
		// Update database finally.
		int count = 0;
		if(table != null) {
			synchronized(mDbHelper) {
				ensureBookTable(db, table);
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
		// Only allow inserting new book sections.
		int matchCode = URI_MATCHER.match(uri);
		if(matchCode != MATCH_BOOK) {
			return null;
		}
		
		// Ensure valid section name.
		String section = values.getAsString(BibleStore.BookCommentColumns.SECTION);
		if(TextUtils.isEmpty(section)) {
			return null;
		}
		
		// Get current database.
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		
		// Update database.
		Uri newUri = null;
		String table = uri.getPathSegments().get(URI_SEGMENT_INDEX_BOOK); // Book name is the table name.
		long rowId = -1;
		synchronized(mDbHelper) {
			ensureBookTable(db, table);
			rowId = db.insert(table, null, values);
		}

		// Generate uri for the new row.
		if(rowId != -1) {
			newUri = Uri.withAppendedPath(uri, section);
			getContext().getContentResolver().notifyChange(uri, null);
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
				table = uri.getPathSegments().get(URI_SEGMENT_INDEX_BOOK); // Book name is the table name.
				where = selection;
			}
			break;
		case MATCH_SECTION:
			{
				table  = uri.getPathSegments().get(URI_SEGMENT_INDEX_BOOK); // Book name is the table name.
				String section = uri.getPathSegments().get(URI_SEGMENT_INDEX_SECTION);
				where = new StringBuilder(
						BibleStore.BookCommentColumns.SECTION).append('=').append(section)
						.append("AND (").append(selection).append(')').toString();
			}
			break;
		default:
			break;
		}
		
		// Update database finally.
		int count = 0;
		if(table != null) {
			synchronized(mDbHelper) {
				ensureBookTable(db, table);
				count = db.delete(table, where, selectionArgs);
			}
			if (count > 0) {
	            getContext().getContentResolver().notifyChange(uri, null);
	        }
		}

        return count;
	}
	
	private void ensureBookTable(SQLiteDatabase db, String table) {
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