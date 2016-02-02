package tk.standy66.deblurit;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class NavigationSpinnerAdapter extends ArrayAdapter<Pair<String, String>> {

	private int textViewResourceId;
	private int dropDownResourceId;
	
	public NavigationSpinnerAdapter(Context context, int textViewResourceId, Pair<String, String>[] objects) {
		super(context, textViewResourceId, objects);
		this.textViewResourceId = textViewResourceId;
	}
	
	@Override
	public void setDropDownViewResource(int resource) {
		// TODO Auto-generated method stub
		dropDownResourceId = resource;
		super.setDropDownViewResource(resource);
	}

	@Override
	public View getDropDownView(int position, View view, ViewGroup parent) {
		Pair<String, String> choice = getItem(position);

	    if (view == null) {
	       LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	       view = vi.inflate(dropDownResourceId, null);
	    }
	    if (choice != null) {
	    	TextView header = (TextView)view.findViewById(R.id.spinner_header);
	    	TextView addinfo = (TextView)view.findViewById(R.id.spinner_addinfo);
	    	header.setText(choice.first);
	    	addinfo.setText(choice.second);
	    }
	    return(view);
	}
	
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		Pair<String, String> choice = getItem(position);

	    if (view == null) {
	       LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	       view = vi.inflate(textViewResourceId, null);
	    }
	    if (choice != null) {
	    	TextView header = (TextView)view.findViewById(R.id.spinner_header);
	    	header.setText(choice.first);
	    }
	    return(view);
	}

}
