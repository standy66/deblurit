package tk.standy66.deblurit;

import tk.standy66.deblurit.R;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.support.v4.app.NavUtils;

public class AboutActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Button thirdparty = (Button)findViewById(R.id.about_thirdparty_button);
        thirdparty.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				startActivity(new Intent(AboutActivity.this, TextViewerActivity.class));
			}
		});
    }    
}
