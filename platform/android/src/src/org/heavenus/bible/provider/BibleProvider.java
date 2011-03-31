package org.heavenus.bible.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

public class BibleProvider extends ContentProvider {
	public static final String AUTHORITY = "org.heavenus.bibleprovider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	
	/* Uri format:
	 *	content://<authority>/<language_name>/<book_category>/<book_name>/<section_name>
	 */
	private static final int LANGUAGES = 1;
	private static final int LANGUAGE_NAME = 2;
	private static final int OLD_BOOKS = 3;
	private static final int NEW_BOOKS = 4;
	private static final int OLD_BOOK_NAME = 5;
	private static final int NEW_BOOK_NAME = 6;
	private static final int OLD_BOOK_SECTION_NAME = 7;
	private static final int NEW_BOOK_SECTION_NAME = 8;
	
	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		URI_MATCHER.addURI(AUTHORITY, null, LANGUAGES);
		URI_MATCHER.addURI(AUTHORITY, "*", LANGUAGE_NAME);
		URI_MATCHER.addURI(AUTHORITY, "*/old", OLD_BOOKS);
		URI_MATCHER.addURI(AUTHORITY, "*/new", NEW_BOOKS);
		URI_MATCHER.addURI(AUTHORITY, "*/old/*", OLD_BOOK_NAME);
		URI_MATCHER.addURI(AUTHORITY, "*/new/*", NEW_BOOK_NAME);
		URI_MATCHER.addURI(AUTHORITY, "*/old/*/*", OLD_BOOK_SECTION_NAME);
		URI_MATCHER.addURI(AUTHORITY, "*/new/*/*", NEW_BOOK_SECTION_NAME);
	}

	@Override
	public String getType(Uri uri) {
		String mimeType = null;

		switch(URI_MATCHER.match(uri)) {
		case LANGUAGES:
			mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/language";
			break;
		case LANGUAGE_NAME:
			mimeType = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/language";
			break;
		case OLD_BOOKS:
			mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/old_book";
			break;
		case NEW_BOOKS:
			mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/new_book";
			break;
		case OLD_BOOK_NAME:
			mimeType = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/old_book";
			break;
		case NEW_BOOK_NAME:
			mimeType = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/new_book";
			break;
		case OLD_BOOK_SECTION_NAME:
			mimeType = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/old_book_section";
			break;
		case NEW_BOOK_SECTION_NAME:
			mimeType = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/new_book_section";
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
		return null;
	}
	
	@Override
	public int update (Uri uri, ContentValues values, String selection, String[] selectionArgs) {
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
}