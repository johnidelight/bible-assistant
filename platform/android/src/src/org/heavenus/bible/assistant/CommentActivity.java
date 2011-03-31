package org.heavenus.bible.assistant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class CommentActivity extends Activity {
	public static final String EXTRA_SECTION_ID = "org.heavenus.bible.assistant.intent.extra.SECTION_ID"; // String
	
	private TextView mComment;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.comment);
        
        mComment = (TextView) findViewById(R.id.comment);

        String id = getSectionId();
        mComment.setText("Comment for " + id);
    }
    
    private String getSectionId() {
    	String sectionId = null;
    	
    	Intent it = getIntent();
        if(Intent.ACTION_VIEW.equals(it.getAction())) {
        	Bundle extra = it.getExtras();
        	if(extra != null) {
        		sectionId = extra.getString(EXTRA_SECTION_ID);
        	}
        }
        
        return sectionId;
    }
}