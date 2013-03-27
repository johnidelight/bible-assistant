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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

/*
 * Base activity to supply common behaviors such as option menus, etc.
 */
public abstract class BaseActivity extends Activity {
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.system_menu, menu);

	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.setting:
			{
				// Show settings.
				Intent it = new Intent(this, SettingActivity.class);
				startActivity(it);
			}
			return true;
		case R.id.about:
			{
				// Show about infos.
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				CharSequence title = new StringBuilder(getResources().getString(R.string.about))
						.append(' ') .append(getResources().getString(R.string.app_name));
				builder.setTitle(title).setIcon(R.drawable.bookmark1).setView(getAboutView())
						.create().show();
			}
			return true;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}
	
	private View getAboutView() {
		View v = View.inflate(this, R.layout.about, null);

		// Get application version.
		TextView tv = (TextView) v.findViewById(R.id.version);
		try {
			PackageManager pm = getPackageManager();
			PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
			tv.setText("V " + info.versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return v;
	}
}