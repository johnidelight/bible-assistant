package org.heavenus.bible.assistant;

import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class BibleAssistantActivity extends ActivityGroup {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        ViewGroup rootLayout = (ViewGroup) findViewById(R.id.root_layout);
        
        // Add book list view.
        Intent it = new Intent(this, BookListActivity.class);
        View view = getLocalActivityManager().startActivity(BookListActivity.class.getName(), it)
        		.getDecorView();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.FILL_PARENT);
        lp.weight = 1;
        rootLayout.addView(view, lp);
        
        // Add book content view.
        it = new Intent(this, BookContentActivity.class);
        view = getLocalActivityManager().startActivity(BookContentActivity.class.getName(), it)
        		.getDecorView();
        lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.FILL_PARENT);
        lp.weight = 3;
        rootLayout.addView(view, lp);
    }
}