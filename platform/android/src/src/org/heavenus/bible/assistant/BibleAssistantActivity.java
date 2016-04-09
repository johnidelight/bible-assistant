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
import java.util.Locale;

import org.heavenus.bible.provider.BibleStore;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class BibleAssistantActivity extends BaseActivity implements AdapterView.OnItemClickListener {
	private class BookCategory {
		public String title;
		public Uri uri;
	}

	private List<BookCategory> mCats;
	private ListView mCatListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        mCatListView = (ListView) findViewById(R.id.category_list);
        
        // Get all book categories.
        mCats = getBookCategories(this);
        if(mCats != null) {
        	List<String> titles = new ArrayList<String>();
        	for(BookCategory cat : mCats) {
        		titles.add(cat.title);
        	}
        	mCatListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, titles));

        	mCatListView.setOnItemClickListener(this);
        }
    }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		BookCategory cat = mCats.get(position);

		// Show books in specific category.
		Intent it = new Intent(Intent.ACTION_VIEW, cat.uri, this, BookListActivity.class);
		it.putExtra(BookListActivity.EXTRA_CATEGORY_TITLE, cat.title);
		startActivity(it);
	}

    private List<BookCategory> getBookCategories(Context c) {
    	List<BookCategory> cats = new ArrayList<BookCategory>();
    	
    	// FIXME: hardcode locale, need use current system locale.
    	Uri uri = Uri.withAppendedPath(BibleStore.BIBLE_CONTENT_URI, Locale.SIMPLIFIED_CHINESE.toString());
    	String[] projection = new String[]{BibleStore.CategoryColumns._ID, BibleStore.CategoryColumns.TITLE};
    	Cursor cursor = c.getContentResolver().query(uri, projection, null, null, null);
    	if(cursor != null) {
    		if(cursor.moveToFirst()) {
    			do {
    				BookCategory cat = new BookCategory();
    				cat.title = cursor.getString(cursor.getColumnIndex(BibleStore.CategoryColumns.TITLE));
    				long id = cursor.getLong(cursor.getColumnIndex(BibleStore.CategoryColumns._ID));
    				cat.uri = ContentUris.withAppendedId(uri, id);

    				cats.add(cat);
    			} while(cursor.moveToNext());
    		}
    		
    		cursor.close();
    	}
    	
    	return cats;
    }
}