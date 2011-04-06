package org.heavenus.bible.assistant;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.heavenus.bible.provider.BibleStore;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

public class BibleAssistantActivity extends Activity {
	private class BookCategory {
		public long id;
		public String title;
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
        	for(BookCategory cat : cats) {
        		Button btn = new Button(this);
        		btn.setText(cat.title);
        		btn.setTag(cat.id);

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
    				cat.id = cursor.getLong(cursor.getColumnIndex(BibleStore.CategoryColumns._ID));
    				cat.title = cursor.getString(cursor.getColumnIndex(BibleStore.CategoryColumns.TITLE));

    				cats.add(cat);
    			} while(cursor.moveToNext());
    		}
    	}
    	
    	return cats;
    }
}