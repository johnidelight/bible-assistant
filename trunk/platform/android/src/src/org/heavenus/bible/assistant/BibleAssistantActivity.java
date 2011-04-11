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

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class BibleAssistantActivity extends Activity {
	private class BookCategory {
		public String title;
		public Uri uri;
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        LinearLayout rootLayout = (LinearLayout) findViewById(R.id.root_layout);
        
        // Fill in all book categories.
        List<BookCategory> cats = getBookCategories(this);
        if(cats != null) {
        	LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
        			LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        	for(final BookCategory cat : cats) {
        		Button btn = new Button(this);
        		btn.setText(cat.title);
        		btn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// Show books in specific category.
						Intent it = new Intent(Intent.ACTION_VIEW, cat.uri,
								BibleAssistantActivity.this, BookListActivity.class);
						it.putExtra(BookListActivity.EXTRA_CATEGORY_TITLE, cat.title);
						startActivity(it);
					}
				});

        		rootLayout.addView(btn, lp);
        	}
        }
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