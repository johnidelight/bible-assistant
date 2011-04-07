package org.heavenus.bible.assistant;

import android.app.ActivityGroup;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class BookActivity extends ActivityGroup {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.book);
        
        LinearLayout rootLayout = (LinearLayout) findViewById(R.id.root_layout);

        // Add book list view.
        Intent it = new Intent(Intent.ACTION_VIEW, getCategoryUri(), this, BookListActivity.class);
        View view = getLocalActivityManager().startActivity(BookListActivity.class.getName(), it)
        		.getDecorView();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.FILL_PARENT);
        lp.weight = 1; // FIXME
        rootLayout.addView(view, lp);
        
        /* TODO: use two-panel UI for large screen; one by one UI for small screen.
        // Add book content view.
        it = new Intent(Intent.ACTION_VIEW, null, this, BookContentActivity.class);
        view = getLocalActivityManager().startActivity(BookContentActivity.class.getName(), it)
        		.getDecorView();
        lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.FILL_PARENT);
        lp.weight = 3;
        rootLayout.addView(view, lp);*/
    }

	private Uri getCategoryUri() {
		Uri uri = null;

		Intent it = getIntent();
		if(Intent.ACTION_VIEW.equals(it.getAction())) {
			uri = it.getData();
		}

		return uri;
	}
}