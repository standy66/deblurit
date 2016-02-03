package tk.standy66.deblurit;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

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

    private static class TextLoaderAsyncTask extends AsyncTask<String, Integer, String> {

        private TextViewerActivity context;
        public TextLoaderAsyncTask(TextViewerActivity context) {
            this.context = context;
        }
        @Override
        protected String doInBackground(String... arg0) {

            try {
                InputStream is = context.getAssets().open(arg0[0]);
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line = br.readLine();
                StringBuilder sb = new StringBuilder();
                while (line != null) {
                    sb.append(line);
                    sb.append("\n");
                    line = br.readLine();
                }
                br.close();
                return sb.toString();
            } catch (Throwable t) {
                return null;
            }

        }

        @Override
        protected void onPostExecute(String result) {
            if (context != null)
                if (result != null)
                    context.text.setText(result);
                else
                    Toast.makeText(context, R.string.toast_error_opening_file, Toast.LENGTH_LONG).show();
            super.onPostExecute(result);
        }

    }
}
