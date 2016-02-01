package tk.standy66.deblurit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import tk.standy66.deblurit.R;

import com.actionbarsherlock.app.SherlockActivity;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class TextViewerActivity extends Activity {

	public TextView text;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_viewer);
        
        Button ok = (Button)findViewById(R.id.textviewer_ok);
        text = (TextView)findViewById(R.id.textviewer_text);
        ok.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				finish();
			}
		});
        text.setText(getResources().getString(R.string.textviewver_loading));
        new TextLoaderAsyncTask(this).execute("LICENSE.txt");
    }  
}
