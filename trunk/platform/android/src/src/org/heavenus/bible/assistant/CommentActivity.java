package org.heavenus.bible.assistant;

import org.heavenus.bible.provider.BibleStore;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

public class CommentActivity extends Activity {
	public static final String EXTRA_SECTION_ID = "org.heavenus.bible.assistant.intent.extra.SECTION_ID"; // String

	private TextView mComment;
	
	private Uri mSectionUri;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.comment);
        
        mComment = (TextView) findViewById(R.id.comment);
        
        mSectionUri = getSectionUri();
        
        // Get current section comment.
        mComment.setText(getComment(this, mSectionUri));
    }
    
    private Uri getSectionUri() {
		Uri uri = null;

		Intent it = getIntent();
		if(Intent.ACTION_VIEW.equals(it.getAction())) {
			uri = it.getData();
		}

		return uri;
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