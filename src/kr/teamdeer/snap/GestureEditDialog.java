package kr.teamdeer.snap;

import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class GestureEditDialog extends Dialog 
implements TextWatcher, View.OnClickListener, OnItemSelectedListener {

	Intent mainIntent;
    PackageManager packageManager;
        
	List<ResolveInfo> apps = null;
	GestureElement target;
	EditText nameInput;
	Spinner spinAction;
	Button submitButton;
	Context context;
	
	String rtName;
	String rtAction;
	OnDismissListener dismiss;
	ArrayAdapter<ResolveInfo> actionAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		packageManager = context.getPackageManager();
		apps = packageManager.queryIntentActivities(mainIntent, 0);
		
		WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();    
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);
		
		setContentView(R.layout.gesture_edit_dialog);
		nameInput = (EditText)findViewById(R.id.nameEditText);
		spinAction = (Spinner)findViewById(R.id.actionSpinner);
		submitButton = (Button)findViewById(R.id.submitButton);
		actionAdapter = 
	            new ArrayAdapter<ResolveInfo>(getContext(), R.layout.activity_item, apps)
	            {
					@Override
					public View getDropDownView(int position, View convertView,
							ViewGroup parent) {
						return getCustomView(position, convertView, parent);
					}
					@Override
	                public View getView(int position, View convertView, ViewGroup parent)
	                {
	                	return getCustomView(position, convertView, parent);
	                }
					
	                public View getCustomView(int position, View convertView, ViewGroup parent)
	                {
	                	if (convertView == null)
	                        convertView = LayoutInflater.from(parent.getContext()).
	                            inflate(R.layout.activity_item, parent, false);
	                    final String text = apps.get(position).activityInfo.
	                        applicationInfo.loadLabel(packageManager).toString()+
	                        " ("+apps.get(position).activityInfo.name+")";
	                    ((TextView)convertView.findViewById(R.id.activityText)).setText(text);
	                    final Drawable drawable = apps.get(position).activityInfo.
	                    	applicationInfo.loadIcon(packageManager);
	                    ((ImageView)convertView.findViewById(R.id.activityImage)).setImageDrawable(drawable);
	                    return convertView;
	                }
	            };
		spinAction.setAdapter(actionAdapter);
		
		nameInput.addTextChangedListener(this);
		spinAction.setOnItemSelectedListener(this);
		submitButton.setOnClickListener(this);
		this.setOnDismissListener(dismiss);
	}
	
	public GestureEditDialog(Context context, OnDismissListener dismiss, GestureElement target) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.dismiss = dismiss;
		this.target = target;
		this.context = context;
	}	

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		rtAction
		= apps.get(position).activityInfo.applicationInfo.packageName
				+ "," + apps.get(position).activityInfo.name;
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		rtAction = "";
	}

	@Override
	public void onClick(View view) {
		target.Name = rtName;
		target.Action = rtAction;
		dismiss();
	}

	@Override
	public void afterTextChanged(Editable s) {
		rtName = s.toString();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {}

}
