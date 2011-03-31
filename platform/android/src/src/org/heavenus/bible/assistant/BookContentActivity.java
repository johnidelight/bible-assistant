package org.heavenus.bible.assistant;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class BookContentActivity extends ListActivity implements ListView.OnItemClickListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, sContents));
        getListView().setOnItemClickListener(this);
    }
    
    private static final String[] sContents = new String[] {
    	"1.1t", "1.1", "1.2", "1.3", "1.4", "2.1", "2.2"
    };

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO: id -> sectonId
		String sectionId = ((TextView) view).getText().toString();
		
		// Show comment.
		Intent it = new Intent(Intent.ACTION_VIEW, null, this, CommentActivity.class);
		it.putExtra(CommentActivity.EXTRA_SECTION_ID, sectionId);
		startActivity(it);
	}
}