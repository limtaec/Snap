package kr.teamdeer.snap;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class SettingActivity extends Activity {

	ListView list;
	GestureListAdapter adapter;
	GestureEditDialog gedlg;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
			
		GestureData.Instance().load(getApplicationContext());
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
	
	public static final int ID_CMENU_EDIT = Menu.FIRST+1;
	public static final int ID_CMENU_DELETE = Menu.FIRST+2;

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.list) {
			AdapterView.AdapterContextMenuInfo info
				= (AdapterView.AdapterContextMenuInfo)menuInfo;
			menu.setHeaderTitle("Edit " + GestureData.Instance().getGesture(info.position).Name);
			menu.add(Menu.NONE, ID_CMENU_EDIT, ID_CMENU_EDIT, "Edit");
			menu.add(Menu.NONE, ID_CMENU_DELETE, ID_CMENU_DELETE, "Delete");
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info
			= (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		final GestureElement target = GestureData.Instance().getGesture(info.position);
		switch (item.getItemId()) {	
		case ID_CMENU_EDIT:
			OnDismissListener dissmissListener = new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					GestureData.Instance().update(getApplicationContext(), target.Id);
				}
		    };
		    gedlg = new GestureEditDialog(this, dissmissListener, target);
		    gedlg.show();
			return true;
			
		case ID_CMENU_DELETE:
			GestureData.Instance().delete(getApplicationContext(), target.Id);
			
		default:
			return super.onContextItemSelected(item);
		}
	}
	
}
