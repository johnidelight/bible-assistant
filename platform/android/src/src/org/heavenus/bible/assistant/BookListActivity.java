package org.heavenus.bible.assistant;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class BookListActivity extends ListActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, sBooks));
    }
    
    private static final String[] sBooks = new String[] {
    	"Book1", "Book2", "Book3"
    };
}