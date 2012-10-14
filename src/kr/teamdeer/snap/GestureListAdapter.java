package kr.teamdeer.snap;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class GestureListAdapter extends BaseAdapter {
	
	private Activity activity;
	private static LayoutInflater inflater;

	public GestureListAdapter(Activity TargetActivity) {
		activity = TargetActivity;
		inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return GestureData.Instance().getGestures().size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		if (convertView == null)
			vi = inflater.inflate(R.layout.gesture_item, null);
		
		TextView name = (TextView)vi.findViewById(R.id.name);
		TextView action = (TextView)vi.findViewById(R.id.action);
		
		name.setText(GestureData.Instance().getGesture(position).Name);
		action.setText(GestureData.Instance().getGesture(position).Action);
		
		return vi;
	}

}
