package org.heavenus.bible.assistant;

import java.util.ArrayList;
import java.util.List;

import org.heavenus.bible.provider.BibleStore;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class BookContentActivity extends Activity implements ListView.OnItemClickListener {
	static final String EXTRA_BOOK_TITLE = "org.heavenus.bible.assistant.intent.extra.BOOK_TITLE"; // String
	
	private ListView mSectionListView;
	
	private class Section {
		public String name;
		public String content;
		
		public int chapterId;
		public int sectionId;

		public boolean isTitle;
		public boolean isChapterTitle;
	}
	private Uri mBookUri;
	private String mBookTitle;
	private List<Section> mSections;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.book_content);
        mSectionListView = (ListView) findViewById(R.id.section_list);
        
        initFromIntent();
        
        // Get current book content.
        mSections = getBookSections(this, mBookUri);
		if(mSections != null) {
			mSectionListView.setAdapter(mSectionAdapter);
			mSectionListView.setOnItemClickListener(this);
		}
    }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// Generate comment uri for current section.
		Section s = mSections.get(position);
		Uri commentUri = Uri.withAppendedPath(BibleStore.BIBLE_COMMENT_CONTENT_URI, BibleStore.getBookName(mBookUri));
		commentUri = Uri.withAppendedPath(commentUri, s.name);
		
		// Show current section comment.
		Intent it = new Intent(Intent.ACTION_VIEW, commentUri, this, CommentActivity.class);
		CharSequence text = ((TextView) view).getText();
		if(s.isChapterTitle) {
			text = new StringBuilder(mBookTitle).append('\n').append(text);
		} else {
			String chapter = getResources().getString(R.string.chapter_title, s.chapterId, ""); // FIXME: lost chapter title,
			text = new StringBuilder(mBookTitle).append('\n').append(chapter).append('\n').append(text);
		}
		it.putExtra(CommentActivity.EXTRA_SECTION_CONTENT, text);
		startActivity(it);
	}
	
	private void initFromIntent() {
		Intent it = getIntent();
		if(Intent.ACTION_VIEW.equals(it.getAction())) {
			mBookUri = it.getData();
			mBookTitle = it.getStringExtra(EXTRA_BOOK_TITLE);
		}
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

    				if(!TextUtils.isEmpty(s.name)) {
    					int index1 = s.name.indexOf('.');
    					try {
    						s.chapterId = Integer.parseInt(s.name.substring(0, index1));
    					} catch(Exception e) {}
    					
    					try {
    						int index2 = s.name.indexOf('t');
    						if(index2 != -1) {
    							s.isTitle = true;
    							s.sectionId = Integer.parseInt(s.name.substring(index1 + 1, index2));
    							s.isChapterTitle = (s.sectionId == 0);
    						} else {
    							s.sectionId = Integer.parseInt(s.name.substring(index1 + 1));
    						}
    					} catch(Exception e) {}
    				}

    				sections.add(s);
    			} while(cursor.moveToNext());
    		}
    		
    		cursor.close();
    	}
		
		return sections;
	}
	
	private BaseAdapter mSectionAdapter = new BaseAdapter() {
		@Override
		public int getCount() {
			return mSections.size();
		}

		@Override
		public Object getItem(int position) {
			return mSections.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView v = null;
			if(convertView != null && convertView instanceof TextView) {
				v = (TextView) convertView;
			} else {
				v = new TextView(BookContentActivity.this);
			}

			// Show different section content formats.
			Section s = mSections.get(position);
			CharSequence text = null;
			if(s.isChapterTitle) {
				text = getResources().getString(R.string.chapter_title, s.chapterId, s.content);
				v.setTypeface(null, Typeface.BOLD);
				v.setTextColor(Color.RED);
			} else if(s.isTitle) {
				text = new StringBuilder("      ").append(s.content); // FIXME: indent
				v.setTypeface(null, Typeface.BOLD);
				v.setTextColor(Color.GREEN);
			} else {
				text = new StringBuilder("  ").append(getResources().getString( // FIXME: indent
						R.string.section_content_general, s.sectionId, s.content));
				v.setTypeface(null, Typeface.NORMAL);
				v.setTextColor(Color.WHITE);
			}
			v.setText(text);
			
			return v;
		}
	};
}