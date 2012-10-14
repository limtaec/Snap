package kr.teamdeer.snap;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class SettingActivity extends Activity {

	SharedPreferences mainPreference;
	ListView list;
	GestureListAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		
		mainPreference = PreferenceManager.getDefaultSharedPreferences(this);
		
		list = (ListView)findViewById(R.id.list);
		adapter = new GestureListAdapter(this);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new ListClickListener());
		this.registerForContextMenu(list);
	}
	
	public class ListClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
			list.showContextMenuForChild(view);
		}
	}
	
	public static final int ID_MENU_ADD = Menu.FIRST+1;
	public static final int ID_MENU_SETTING = Menu.FIRST+2;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, ID_MENU_ADD, ID_MENU_ADD, "Add");
		menu.add(Menu.NONE, ID_MENU_SETTING, ID_MENU_SETTING, "Settings");
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case ID_MENU_ADD:
			startActivity(new Intent(this, GestureLearningActivity.class));
			return true;
			
		case ID_MENU_SETTING:
			startActivity(new Intent(this, AdvancedSettingActivity.class));
			return true;
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public static final int ID_CMENU_NAME = Menu.FIRST+1;
	public static final int ID_CMENU_ACTION = Menu.FIRST+2;
	public static final int ID_CMENU_DELETE = Menu.FIRST+3;

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.list) {
			AdapterView.AdapterContextMenuInfo info
				= (AdapterView.AdapterContextMenuInfo)menuInfo;
			menu.setHeaderTitle("Edit " + GestureData.Instance().getGesture(info.position).Name);
			menu.add(Menu.NONE, ID_CMENU_NAME, ID_CMENU_NAME, "Rename");
			menu.add(Menu.NONE, ID_CMENU_ACTION, ID_CMENU_ACTION, "Select Action");
			menu.add(Menu.NONE, ID_CMENU_DELETE, ID_CMENU_DELETE, "Delete");
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info
			= (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		final GestureElement target = GestureData.Instance().getGesture(info.position);
		AlertDialog.Builder dlg = new AlertDialog.Builder(this);
		switch (item.getItemId()) {
		case ID_CMENU_NAME:
			dlg.setTitle("Enter New Name");
			final EditText input = new EditText(this);
			input.setText(target.Name);
			dlg.setView(input);
			dlg.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (input.getText().length()>0)
						target.Name = input.getText().toString();
				}
			});
			dlg.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {}
			});
			dlg.show();
			return true;
			
		case ID_CMENU_ACTION:
			dlg.setTitle("Select Action");
			final ListView actionListView = new ListView(this);
			dlg.setView(actionListView);
			final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
	        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
	        
	        final PackageManager packageManager = getPackageManager();
	        final List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
	        
	        final ArrayAdapter<ResolveInfo> actionAdapter = 
	                new ArrayAdapter<ResolveInfo>(this, R.layout.activity_item, apps)
	                {
	                    @Override
	                    public View getView(int position, View convertView, ViewGroup parent)
	                    {
	                    	if (convertView == null)
	                            convertView = LayoutInflater.from(parent.getContext()).
	                                inflate(R.layout.activity_item, parent, false);
	                        final String text = apps.get(position).activityInfo.
	                            applicationInfo.loadLabel(packageManager).toString();
	                        ((TextView)convertView.findViewById(R.id.text)).setText(text);
	                        final Drawable drawable = apps.get(position).activityInfo.applicationInfo.loadIcon(packageManager);
	                        ((ImageView)convertView.findViewById(R.id.image)).setImageDrawable(drawable);
	                        return convertView;
	                    }
	                };
	        actionListView.setAdapter(actionAdapter);
	        actionListView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					target.Action
						= apps.get(position).activityInfo.applicationInfo.packageName
						+ apps.get(position).activityInfo.name;
				}
	        });
			return true;
			
		case ID_CMENU_DELETE:
			GestureData.Instance().getGestures().remove(info.position);
			
		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	protected void onPause() {
		startService(new Intent(this, GestureRecognizeService.class));
		super.onPause();
	}

	@Override
	protected void onResume() {
		stopService(new Intent(this, GestureRecognizeService.class));
		super.onResume();
	}
	
}
