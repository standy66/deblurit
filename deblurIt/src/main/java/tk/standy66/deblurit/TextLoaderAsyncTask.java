package tk.standy66.deblurit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import tk.standy66.deblurit.R;

import android.os.AsyncTask;
import android.widget.Toast;

public class TextLoaderAsyncTask extends AsyncTask<String, Integer, String> {

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
