package tk.standy66.deblurit;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

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
