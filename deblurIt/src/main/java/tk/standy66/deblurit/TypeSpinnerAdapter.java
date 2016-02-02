package tk.standy66.deblurit;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import tk.standy66.deblurit.tools.Image;

public class TypeSpinnerAdapter extends ArrayAdapter<String> {

	private int textViewResourceId;
	private View lastViewSelected;
	
	public void setKernelPreview(Image image) {
		if (lastViewSelected == null)
			return;
		ImageView iv = (ImageView)lastViewSelected.findViewById(R.id.type_spinner_image);
		iv.setImageBitmap(image.toBitmap());
	}
	
	public TypeSpinnerAdapter(Context context, int textViewResourceId, String[] objects) {
		super(context, textViewResourceId, objects);
		this.textViewResourceId = textViewResourceId;
	}

	final static int[] images = { R.drawable.circle, R.drawable.gaussian, R.drawable.line };
	
	@Override
	public View getDropDownView(int position, View view, ViewGroup parent) {
		String choice = getItem(position);

	    if (view == null) {
	       LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	       view = vi.inflate(textViewResourceId, null);
	    }
	    if (choice != null) {
	    	TextView text = (TextView)view.findViewById(R.id.type_spinner_text);
	    	ImageView iv = (ImageView)view.findViewById(R.id.type_spinner_image);
	    	text.setText(choice);
	    	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
	    		text.setTextColor(Color.BLACK);
	    	else
	    		text.setTextColor(Color.WHITE);
	    	iv.setImageResource(images[position]);
	    }
	    return(view);
	}
	
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		return lastViewSelected = getDropDownView(position, view, parent);
	}

}
