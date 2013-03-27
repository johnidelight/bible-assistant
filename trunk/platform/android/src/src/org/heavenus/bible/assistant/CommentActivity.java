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

import org.heavenus.bible.provider.BibleStore;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

public class CommentActivity extends BaseActivity {
	static final String EXTRA_BOOK_TITLE = "org.heavenus.bible.assistant.intent.extra.BOOK_TITLE"; // String
	static final String EXTRA_SECTION_CONTENT = "org.heavenus.bible.assistant.intent.extra.SECTION_CONTENT"; // String

	private TextView mSectionView;
	private EditText mCommentView;
	
	private Uri mSectionUri;
	private String mBookTitle;
	private String mSectionName;
	private String mSectionContent;

	private String mComment;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.comment);
        
        mSectionView = (TextView) findViewById(R.id.section);
        mCommentView = (EditText) findViewById(R.id.comment);
        
        initFromIntent();

        setTitle(mBookTitle);
        mSectionView.setText(mSectionContent);

        mComment = getComment(this, mSectionUri);
        if(mComment != null) {
        	mCommentView.setText(mComment);
        }
    }

    @Override
	protected void onPause() {
		super.onPause();

		// Save new comment here.
		saveComment(mCommentView.getText().toString());
	}

	private void initFromIntent() {
		Intent it = getIntent();
		if(Intent.ACTION_VIEW.equals(it.getAction())) {
			mSectionUri = it.getData();
			mSectionName = BibleStore.getSectionName(mSectionUri);
			mBookTitle = it.getStringExtra(EXTRA_BOOK_TITLE);
			mSectionContent = it.getStringExtra(EXTRA_SECTION_CONTENT);
		}
    }
    
    private String getComment(Context c, Uri sectionUri) {
		if(sectionUri == null) return null;
		
		String comment = null;

    	String[] projection = new String[]{BibleStore.BookCommentColumns.COMMENT};
    	Cursor cursor = c.getContentResolver().query(sectionUri, projection, null, null, null);
    	if(cursor != null) {
    		if(cursor.moveToFirst()) {
    			comment = cursor.getString(cursor.getColumnIndex(BibleStore.BookCommentColumns.COMMENT));
    		}
    		
    		cursor.close();
    	}
		
		return comment;
    }
    
    private void saveComment(String newComment) {
    	if(mComment != null) {
			if(!mComment.equals(newComment)) {
				mComment = newComment;

				ContentValues values = new ContentValues();
				values.put(BibleStore.BookCommentColumns.COMMENT, mComment);
				getContentResolver().update(mSectionUri, values, null, null);
			}
		} else {
			if(!TextUtils.isEmpty(newComment)) {
				mComment = newComment;

				ContentValues values = new ContentValues();
				values.put(BibleStore.BookCommentColumns.SECTION, mSectionName);
				values.put(BibleStore.BookCommentColumns.COMMENT, mComment);
				getContentResolver().insert(mSectionUri, values);
			}
		}
    }
}