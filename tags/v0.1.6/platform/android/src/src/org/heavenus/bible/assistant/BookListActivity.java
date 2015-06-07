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

package org.heavenus.bible.assistant;

import java.util.ArrayList;
import java.util.List;

import org.heavenus.bible.provider.BibleStore;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class BookListActivity extends BaseActivity implements AdapterView.OnItemClickListener {
	static final String EXTRA_CATEGORY_TITLE = "org.heavenus.bible.assistant.intent.extra.CATEGORY_TITLE"; // String
	
	private ListView mBookListView;
	
	private class Book {
		public String name;
		public String title;
		public String titleShort;
	}
	private Uri mCategoryUri;
	private String mCategoryTitle;
	private List<Book> mBooks;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.book_list);
        mBookListView = (ListView) findViewById(R.id.book_list);

        initFromIntent();
        
        setTitle(mCategoryTitle);
        
        // Get all books in current category.
        mBooks = getBooks(this, mCategoryUri);
        if(mBooks != null) {
        	List<String> titles = new ArrayList<String>();
        	for(Book b : mBooks) {
        		titles.add(b.title);
        	}
        	mBookListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, titles));
        	
        	mBookListView.setOnItemClickListener(this);
        }
    }

    @Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// Generate current book uri.
    	Book book = mBooks.get(position);
		Uri bookUri = Uri.withAppendedPath(mCategoryUri, book.name);
		
		// Show current book content.
		Intent it = new Intent(Intent.ACTION_VIEW, bookUri, this, BookContentActivity.class);
		it.putExtra(BookContentActivity.EXTRA_BOOK_TITLE, book.title);
		it.putExtra(BookContentActivity.EXTRA_BOOK_TITLE_SHORT, book.titleShort);
		startActivity(it);
	}

	private void initFromIntent() {
		Intent it = getIntent();
		if(Intent.ACTION_VIEW.equals(it.getAction())) {
			mCategoryUri = it.getData();
			mCategoryTitle = it.getStringExtra(EXTRA_CATEGORY_TITLE);
		}
	}
	
	private List<Book> getBooks(Context c, Uri categoryUri) {
		if(categoryUri == null) return null;

		List<Book> books = new ArrayList<Book>();

    	String[] projection = new String[] {
    			BibleStore.BookColumns.NAME, BibleStore.BookColumns.TITLE, BibleStore.BookColumns.TITLE_SHORT };
    	Cursor cursor = c.getContentResolver().query(categoryUri, projection, null, null, null);
    	if(cursor != null) {
    		if(cursor.moveToFirst()) {
    			do {
    				Book book = new Book();
    				book.name = cursor.getString(cursor.getColumnIndex(BibleStore.BookColumns.NAME));
    				book.title = cursor.getString(cursor.getColumnIndex(BibleStore.BookColumns.TITLE));
    				book.titleShort = cursor.getString(cursor.getColumnIndex(BibleStore.BookColumns.TITLE_SHORT));

    				books.add(book);
    			} while(cursor.moveToNext());
    		}
    		
    		cursor.close();
    	}
		
		return books;
	}
}