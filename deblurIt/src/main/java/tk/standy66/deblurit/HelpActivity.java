package tk.standy66.deblurit;

import tk.standy66.deblurit.R;

import com.actionbarsherlock.app.SherlockActivity;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.text.Html;

public class HelpActivity extends SherlockActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        TextView main = (TextView)findViewById(R.id.help_textview);
        main.setText(Html.fromHtml(getResources().getString(R.string.help_message), new Html.ImageGetter() {			
			public Drawable getDrawable(String source) {
				Drawable drawable = getResources().getDrawable(R.drawable.circle);
				drawable.setBounds(0, 0, 50, 50);
				return drawable;
				/*int id = 0;
				if (source.equals("circle"))
					id = R.drawable.circle;
				if (id == 0)
					return null;
				Drawable d = getResources().getDrawable(id);
				return d;*/
			}
		}, null));
    }
    
}
