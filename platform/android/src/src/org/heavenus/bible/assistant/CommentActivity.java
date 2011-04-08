package org.heavenus.bible.assistant;

import org.heavenus.bible.provider.BibleStore;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

public class CommentActivity extends Activity {
	static final String EXTRA_SECTION_CONTENT = "org.heavenus.bible.assistant.intent.extra.SECTION_CONTENT"; // String

	private TextView mSectionView;
	private EditText mCommentView;
	
	private Uri mSectionUri;
	private String mSectionName;
	private String mSectionContent;
	
	private boolean mHasCommentRecord = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.comment);
        
        mSectionView = (TextView) findViewById(R.id.section);
        mCommentView = (EditText) findViewById(R.id.comment);
        
        initFromIntent();

        mSectionView.setText(mSectionContent);

        String comment = getComment(this, mSectionUri);
        if(comment != null) {
        	mHasCommentRecord = true;
        	mCommentView.setText(comment);
        }
    }

    @Override
	protected void onPause() {
		super.onPause();
		
		// Save comment.
		if(mHasCommentRecord) {
			ContentValues values = new ContentValues();
			values.put(BibleStore.BookCommentColumns.COMMENT, mCommentView.getText().toString());
			getContentResolver().update(mSectionUri, values, null, null);
		} else {
			ContentValues values = new ContentValues();
			values.put(BibleStore.BookCommentColumns.SECTION, mSectionName);
			values.put(BibleStore.BookCommentColumns.COMMENT, mCommentView.getText().toString());
			mHasCommentRecord = (getContentResolver().insert(mSectionUri, values) != null);
		}
	}

	private void initFromIntent() {
		Intent it = getIntent();
		if(Intent.ACTION_VIEW.equals(it.getAction())) {
			mSectionUri = it.getData();
			mSectionName = BibleStore.getSectionName(mSectionUri);
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
}