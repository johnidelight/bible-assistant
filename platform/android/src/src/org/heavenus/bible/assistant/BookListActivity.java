package org.heavenus.bible.assistant;

import java.util.ArrayList;
import java.util.List;

import org.heavenus.bible.provider.BibleStore;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class BookListActivity extends Activity implements AdapterView.OnItemClickListener {
	private ListView mBookListView;
	
	private class Book {
		public String name;
		public String title;
	}
	private Uri mCategoryUri;
	private List<Book> mBooks;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.book_list);
        mBookListView = (ListView) findViewById(R.id.book_list);

        initFromIntent();
        
        // Get all books in current category.
        mBooks = getBooks(this, mCategoryUri);
        if(mBooks != null) {
        	List<String> titles = new ArrayList<String>();
        	for(Book b : mBooks) {
        		titles.add(b.title);
        	}
        	mBookListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, titles));
        	
        	mBookListView.setOnItemClickListener(this);
        }
    }

    @Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// Generate current book uri.
    	Book book = mBooks.get(position);
		Uri bookUri = Uri.withAppendedPath(mCategoryUri, book.name);
		
		// Show current book content.
		Intent it = new Intent(Intent.ACTION_VIEW, bookUri, this, BookContentActivity.class);
		it.putExtra(BookContentActivity.EXTRA_BOOK_TITLE, book.title);
		startActivity(it);
	}

	private void initFromIntent() {
		Intent it = getIntent();
		if(Intent.ACTION_VIEW.equals(it.getAction())) {
			mCategoryUri = it.getData();
		}
	}
	
	private List<Book> getBooks(Context c, Uri categoryUri) {
		if(categoryUri == null) return null;

		List<Book> books = new ArrayList<Book>();

    	String[] projection = new String[]{BibleStore.BookColumns.NAME, BibleStore.BookColumns.TITLE};
    	Cursor cursor = c.getContentResolver().query(categoryUri, projection, null, null, null);
    	if(cursor != null) {
    		if(cursor.moveToFirst()) {
    			do {
    				Book book = new Book();
    				book.name = cursor.getString(cursor.getColumnIndex(BibleStore.BookColumns.NAME));
    				book.title = cursor.getString(cursor.getColumnIndex(BibleStore.BookColumns.TITLE));

    				books.add(book);
    			} while(cursor.moveToNext());
    		}
    		
    		cursor.close();
    	}
		
		return books;
	}
}