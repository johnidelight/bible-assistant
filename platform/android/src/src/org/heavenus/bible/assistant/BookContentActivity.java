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

import org.heavenus.bible.provider.BibleStore;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BookContentActivity extends BaseActivity implements ListView.OnItemClickListener {
	static final String EXTRA_BOOK_TITLE		= "org.heavenus.bible.assistant.intent.extra.BOOK_TITLE";		// String
	static final String EXTRA_BOOK_TITLE_SHORT	= "org.heavenus.bible.assistant.intent.extra.BOOK_TITLE_SHORT";	// String

	private ListView mSectionListView;
	
	private class Section {
		public String name;
		public String content;

		public boolean isTitle;
		public boolean isMainTitle;
		public boolean isChapterTitle;
		
		public boolean visableSectionName;
	}
	private Uri mBookUri;
	private String mBookTitle;
	private String mBookTitleShort;
	private int mBookMarkSectionPos = -1;

	private SectionAdapter mSectionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.book_content);

        mSectionListView = (ListView) findViewById(R.id.section_list);
        mSectionListView.setOnItemClickListener(this);
        registerForContextMenu(mSectionListView);

        initFromIntent();
        setTitle(mBookTitle);

		// Initialize book sections.
    	mSectionAdapter = new SectionAdapter(this, mBookUri);
    	mSectionListView.setAdapter(mSectionAdapter);

        // Scroll to the current book mark section.
        mBookMarkSectionPos = getBookMarkSectionPosition(mBookUri);
        mSectionListView.setSelectionAfterHeaderView();
        mSectionListView.setSelection(mBookMarkSectionPos);
    }
    
	private void initFromIntent() {
		Intent it = getIntent();
		if(Intent.ACTION_VIEW.equals(it.getAction())) {
			mBookUri = it.getData();
			mBookTitle = it.getStringExtra(EXTRA_BOOK_TITLE);
			mBookTitleShort = it.getStringExtra(EXTRA_BOOK_TITLE_SHORT);
		}
	}
    
	@Override
	protected void onStart() {
		super.onStart();

		if(mSectionAdapter != null) {
			mSectionAdapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();

		// Save the current reading section to book mark.
		mBookMarkSectionPos = mSectionListView.getFirstVisiblePosition();
		if(mBookMarkSectionPos >= 0) {
			Section s = (Section)mSectionAdapter.getItem(mBookMarkSectionPos);
			if (s != null) {
				saveBookMarkSection(mBookUri, mBookMarkSectionPos + 1, s.name); // _ID starts from 1
			}
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.section_menu, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    switch(item.getItemId()) {
	    	case R.id.share:
	    		shareSectionContent(info.position);
	    		return true;
	        case R.id.copy:
	        	copySectionContent(info.position);
	            return true;
	        case R.id.add_comments:
	        	startCommentActivity(info.position);
	            return true;
	        default:
	            return super.onContextItemSelected(item);
	    }
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// Not show comment if direct comment mode is disabled.
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		if(pref != null) {
			boolean enabled = pref.getBoolean(getResources().getString(R.string.key_comment_enable_direct_comment),
					getResources().getBoolean(R.bool.default_value_comment_enable_direct_comment));
			if(!enabled) return;
		}

		startCommentActivity(position);
	}
	
	private void startCommentActivity(int sectionPosition) {
		// Get section info.
		Section s = (Section)mSectionAdapter.getItem(sectionPosition);
		Uri sectionUri = Uri.withAppendedPath(mBookUri, s.name);
		CharSequence sectionText = getSectionContentInBookMode(s);

		// Show current section comment.
		Intent it = new Intent(Intent.ACTION_VIEW, sectionUri, this, CommentActivity.class);
		it.putExtra(CommentActivity.EXTRA_BOOK_TITLE, mBookTitle);
		it.putExtra(CommentActivity.EXTRA_SECTION_CONTENT, sectionText);
		startActivity(it);
	}

	private void shareSectionContent(int sectionPosition) {
		Section s = (Section)mSectionAdapter.getItem(sectionPosition);
		String content = getSectionContentInShareMode(s);

		Intent it = new Intent();
		it.setAction(Intent.ACTION_SEND);
		it.putExtra(Intent.EXTRA_TEXT, content);
		it.setType("text/plain");
		startActivity(Intent.createChooser(it, getText(R.string.share)));
	}
	
	private void copySectionContent(int sectionPosition) {
		Section s = (Section)mSectionAdapter.getItem(sectionPosition);
		String content = getSectionContentInShareMode(s);

    	ClipboardManager board = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
    	board.setText(content);
    	if (board.hasText()) {
    		Toast.makeText(this, R.string.copied, Toast.LENGTH_LONG).show();
    	}
	}

	private int getBookMarkSectionPosition(Uri bookUri) {
		int pos = -1;

		String bookName = BibleStore.getBookName(bookUri);
		Uri bookMarkUri = Uri.withAppendedPath(BibleStore.BIBLE_MARK_CONTENT_URI, bookName);

		String[] projection = new String[] { BibleStore.BookMarkColumns.SECTION_ID, BibleStore.BookMarkColumns.SECTION_NAME };
		String where = new StringBuilder(
				BibleStore.BookMarkColumns.BOOK_NAME).append("=\'").append(bookName).append('\'').toString();
		Cursor cursor = getContentResolver().query(bookMarkUri, projection, where, null, null);
		if(cursor != null) {
    		if(cursor.moveToFirst()) {
    			pos = cursor.getInt(cursor.getColumnIndex(BibleStore.BookMarkColumns.SECTION_ID)) - 1; // _ID starts from 1
    		}
    		
    		cursor.close();
		}
		
		return pos;
	}
	
	private void saveBookMarkSection(Uri bookUri, int sectionId, String sectionName) {
		String bookName = BibleStore.getBookName(bookUri);
		Uri bookMarkUri = Uri.withAppendedPath(BibleStore.BIBLE_MARK_CONTENT_URI, bookName);
		
		ContentValues values = new ContentValues();
		values.put(BibleStore.BookMarkColumns.BOOK_NAME, bookName);
		values.put(BibleStore.BookMarkColumns.SECTION_ID, sectionId);
		values.put(BibleStore.BookMarkColumns.SECTION_NAME, sectionName);
		getContentResolver().insert(bookMarkUri, values);
	}
	
	private String getSectionContentInBookMode(Section s) {
		String content = s.content;
		if(s.visableSectionName) {
			content = getResources().getString(R.string.section_main_body, s.name, s.content);
		}
		
		return content;
	}
	
	private String getSectionContentInShareMode(Section s) {
		String content = s.content;
		if(s.visableSectionName) {
			content = getResources().getString(R.string.section_content_for_share_full, s.content, mBookTitleShort, s.name);
		} else {
			content = getResources().getString(R.string.section_content_for_share, s.content, mBookTitleShort);
		}
		
		return content;
	}

	private class SectionAdapter extends BaseAdapter {
		private Context mContext;
		private Uri mBookUri;
		private ArrayList<Section> mSections;
		
		public SectionAdapter(Context context, Uri bookUri) {
			mContext = context;
			mBookUri = bookUri;

			int count = getSectionCount(mBookUri);
			mSections = new ArrayList<Section>(count);
			for(int i = 0; i < count; i++) {
				mSections.add(null);
			}
		}

		@Override
		public int getCount() {
			return mSections.size();
		}

		@Override
		public Object getItem(int position) {
			return ensureSection(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;
			
			// Format sections content using different layouts.
			// TODO: reuse convertView.
			Section s = ensureSection(position);
			if(s.isMainTitle) {
				view = View.inflate(mContext, R.layout.section_main_title, null);
			} else if(s.isChapterTitle) {
				view = View.inflate(mContext, R.layout.section_chapter_title, null);
			} else if(s.isTitle) {
				view = View.inflate(mContext, R.layout.section_part_title, null);
			} else { // Main body
				view = View.inflate(mContext, R.layout.section_main_body, null);
			}
			
			TextView tv = (TextView) view.findViewById(android.R.id.text1);
			if(tv != null) {
				CharSequence text = getSectionContentInBookMode(s);
				tv.setText(text);

				// Show indicator for sections with comments.
				Uri sectionUri = Uri.withAppendedPath(mBookUri, s.name);
				String comment = CommentActivity.getComment(mContext, sectionUri);
				if(!TextUtils.isEmpty(comment)) {
					Drawable indicator = mContext.getResources().getDrawable(R.drawable.bookmark4);
					tv.setCompoundDrawablesWithIntrinsicBounds(null, null, indicator, null);
				}
			}

			return view;
		}
		
		private int getSectionCount(Uri bookUri) {
			if(bookUri == null)
				return 0;

			int count = 0;
	    	String[] projection = new String[] { "COUNT(" + BibleStore.BookContentColumns.SECTION + ")" };
	    	Cursor cursor = getContentResolver().query(bookUri, projection, null, null, null);
	    	if(cursor != null) {
	    		if(cursor.moveToFirst()) {
    				count = cursor.getInt(0);
	    		}
	    	}
			
			return count;
		}
		
		private Section ensureSection(int position) {
			Section s = mSections.get(position);
			if(s != null)
				return s;

	    	String[] projection = new String[] { BibleStore.BookContentColumns.SECTION, BibleStore.BookContentColumns.CONTENT };
	    	String selection = BibleStore.BookContentColumns._ID + "=" + String.valueOf(position + 1); // _ID starts from 1
	    	Cursor cursor = getContentResolver().query(mBookUri, projection, selection, null, null);
	    	if(cursor != null) {
	    		if(cursor.moveToFirst()) {
    				// Initialize every section.
    				//
    				s = new Section();
    				s.name = cursor.getString(cursor.getColumnIndex(BibleStore.BookContentColumns.SECTION));
    				s.content = cursor.getString(cursor.getColumnIndex(BibleStore.BookContentColumns.CONTENT));
    				if(s.content == null) {
    					s.content = "";
    				}

    				// Parse section name to get details.
    				s.visableSectionName = true;
    				if(!TextUtils.isEmpty(s.name) && s.name.matches(BibleStore.SECTION_NAME_REGEX)) {
    					try {
    						// chapter
    						int index = s.name.indexOf(':');
    						String chapter = s.name.substring(0, index);
    						char end = chapter.charAt(chapter.length() - 1);
    						if (end >= 'a' && end <= 'z') { // 1a
    							s.visableSectionName = false;
    						}

    						// sentence
    						String sentence = s.name.substring(index + 1);
    						index = sentence.indexOf('t');
    						if(index != -1) { // 1tt
    							s.isTitle = true;
    							s.visableSectionName = false;

    							int sentenceId = Integer.parseInt(sentence.substring(0, index));
    							s.isMainTitle = (sentenceId < 0); // -1t
    							s.isChapterTitle = (sentenceId == 0); // 0t
    						} else { // body
    							end = sentence.charAt(sentence.length() - 1);
    							if (end >= 'a' && end <= 'z') { // 1x
    								s.visableSectionName = false;
    							} else {
    								int sentenceId = Integer.parseInt(sentence);
    								if (sentenceId <= 0) {
    									s.visableSectionName = false;
    								}
    							}
    						}
    					} catch(Exception e) {}
    				} else {
    					s.visableSectionName = false;
    				}
	    		}
	    		
	    		cursor.close();
	    	}
	    	
	    	mSections.set(position, s);
			return s;
		}
	}
}