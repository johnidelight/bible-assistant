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

package org.heavenus.bible.generator;

import java.io.File;
import java.io.FileFilter;

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import org.heavenus.bible.provider.BibleStore;;

public class BibleGeneratorActivity extends Activity {
	private static final String BOOKS_DIR = "/sdcard/bible";
	private static final String BOOK_EXTENSION = ".txt";
	private static final String DATABASE_PATH = BOOKS_DIR + "/bible.db";
	
	private Button mBtnGenerate;
	private ListView mMessageList;
	
	private ArrayAdapter<String> mMessageAdapter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        
        mBtnGenerate = (Button) findViewById(R.id.generate);
        mMessageList = (ListView) findViewById(R.id.message);
        
        mBtnGenerate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				(new BibleTask()).execute();
			}
		});

        mMessageAdapter = new ArrayAdapter<String>(this, R.layout.list_item);
        mMessageList.setAdapter(mMessageAdapter);
        mMessageList.setSelectionAfterHeaderView();
    }
    
    private class BibleTask extends AsyncTask<Void, String, String> {
    	@Override
		protected String doInBackground(Void... params) {
			return generate();
		}
    	
    	@Override
		protected void onPreExecute() {
			mBtnGenerate.setEnabled(false);
			mMessageAdapter.clear();
		}

		@Override
		protected void onProgressUpdate(String... values) {
			mMessageAdapter.add(values[0]);
			mMessageList.setSelection(mMessageAdapter.getCount() - 1);
			
		}

		@Override
		protected void onPostExecute(String result) {
			mMessageAdapter.add(result);
			mMessageList.setSelection(mMessageAdapter.getCount() - 1);
			mBtnGenerate.setEnabled(true);
		}

		private String generate() {
			int total = 0, succeeded = 0;
			
			// Get all text files in books directory.
			File root = new File(BOOKS_DIR);
			if(root.exists() && root.isDirectory()) {
				File[] files = root.listFiles(new FileFilter() {
					@Override
					public boolean accept(File file) {
						return (!file.isHidden()
								&& file.getName().toLowerCase().endsWith(BOOK_EXTENSION));
					}
				});
				
				// Import every book content to database table.
				if(files != null) {
					total = files.length;
					publishProgress("Found book count: " + total);
					
					for(int i = 0; i < total; i ++) {
						publishProgress(new StringBuilder("Start processing book ").append(i + 1)
								.append(": ").append(files[i].getName()).toString());
						try {
							bookToTable(new Book(files[i]));
						} catch(Exception e) {
							e.printStackTrace();
							publishProgress("Failed!");
							continue;
						}
						
						publishProgress("Succeeded!");
					}
				}
			}
			
			return new StringBuilder("Total books: ").append(total)
					.append("; Succeeded: ").append(succeeded)
					.append("; Failed: ").append(total - succeeded).toString();
		}
		
		private void bookToTable(Book b) throws SQLiteException {
			SQLiteDatabase db = null;
			try {
				db = SQLiteDatabase.openDatabase(DATABASE_PATH, null,
						SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.CREATE_IF_NECESSARY);
				
				// Create new book table.
				// Book file name(without extension) will be used as table name.
				String table = b.getName();
				db.execSQL("DROP TABLE IF EXISTS " + table);
				db.execSQL(new StringBuilder("CREATE TABLE ").append(table).append(" (")
						.append(BibleStore.BookContentColumns._ID).append(" INTEGER PRIMARY KEY,")
						.append(BibleStore.BookContentColumns.SECTION).append(" TEXT,")
						.append(BibleStore.BookContentColumns.CONTENT).append(" TEXT")
						.append(");").toString());
				
				// Add all book sections to current table.
				db.beginTransaction();
				try {
					int count = b.getSectionCount();
					for(int i = 0; i < count ; i ++) {
						// Ensure valid section name first.
						Book.Section s = b.getSection(i);
						if(TextUtils.isEmpty(s.name)) {
							publishProgress(new StringBuilder("Section ")
									.append(i + 1).append(" name cannot be empty!").toString());
							throw new SQLiteException("Failed to insert section record!");
						}

						ContentValues values = new ContentValues();
						values.put(BibleStore.BookContentColumns.SECTION, s.name);
						values.put(BibleStore.BookContentColumns.CONTENT, s.content);
						if(db.insert(table, null, values) == -1) {
							publishProgress(new StringBuilder("Section ")
									.append(i + 1).append(" has error!").toString());
							throw new SQLiteException("Failed to insert section record!");
						}
					}

					db.setTransactionSuccessful();
				} finally {
					db.endTransaction();
				}
			} finally {
				if(db != null) {
					db.close();
				}
			}
		}
    };
}