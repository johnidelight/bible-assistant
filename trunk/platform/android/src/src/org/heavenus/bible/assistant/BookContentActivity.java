package org.heavenus.bible.assistant;

import java.util.ArrayList;
import java.util.List;

import org.heavenus.bible.provider.BibleStore;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class BookContentActivity extends Activity implements ListView.OnItemClickListener {
	private ListView mSectionListView;
	
	private class Section {
		public String name;
		public String content;
	}
	private Uri mBookUri;
	private List<Section> mSections;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.book_content);
        mSectionListView = (ListView) findViewById(R.id.section_list);
        
        mBookUri = getBookUri();
        
        // Get current book content.
        mSections = getBookSections(this, mBookUri);
		if(mSections != null) {
			List<String> contents = new ArrayList<String>();
			for(Section s : mSections) {
				contents.add(new StringBuilder(s.name).append(' ').append(s.content).toString());
			}
			mSectionListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contents));

			mSectionListView.setOnItemClickListener(this);
		}
    }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// Generate comment uri for current section.
		Uri commentUri = Uri.withAppendedPath(BibleStore.BIBLE_COMMENT_CONTENT_URI, BibleStore.getBookName(mBookUri));
		commentUri = Uri.withAppendedPath(commentUri, mSections.get(position).name);
		
		// Show current section comment.
		Intent it = new Intent(Intent.ACTION_VIEW, commentUri, this, CommentActivity.class);
		startActivity(it);
	}
	
	private Uri getBookUri() {
		Uri uri = null;

		Intent it = getIntent();
		if(Intent.ACTION_VIEW.equals(it.getAction())) {
			uri = it.getData();
		}

		return uri;
	}
	
	private List<Section> getBookSections(Context c, Uri bookUri) {
		if(bookUri == null) return null;

		List<Section> sections = new ArrayList<Section>();

    	String[] projection = new String[]{BibleStore.BookContentColumns.SECTION, BibleStore.BookContentColumns.CONTENT};
    	Cursor cursor = c.getContentResolver().query(bookUri, projection, null, null, null);
    	if(cursor != null) {
    		if(cursor.moveToFirst()) {
    			do {
    				Section s = new Section();
    				s.name = cursor.getString(cursor.getColumnIndex(BibleStore.BookContentColumns.SECTION));
    				s.content = cursor.getString(cursor.getColumnIndex(BibleStore.BookContentColumns.CONTENT));

    				sections.add(s);
    			} while(cursor.moveToNext());
    		}
    		
    		cursor.close();
    	}
		
		return sections;
	}
}