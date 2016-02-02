package tk.standy66.deblurit;

import android.os.AsyncTask;

import tk.standy66.deblurit.filtering.Pipeline;
import tk.standy66.deblurit.tools.Image;
import tk.standy66.deblurit.tools.LibImageFilters;

public class DeconvolutionAsyncTask extends AsyncTask<Object, Integer, Image> {

	private PreviewActivity context;
	
	public DeconvolutionAsyncTask(PreviewActivity context) {
		this.context = context;
	}
	@Override
	protected Image doInBackground(Object... arg0) {
		Pipeline p = (Pipeline)arg0[0];
		p.run();
		return p.getImage();
	}
	
	@Override
	protected void onPreExecute() {
		LibImageFilters.registerSubscriber(DeconvolutionAsyncTask.class, this, "onProgressUpdate", "(I)I");
		super.onPreExecute();
	}
	
	public int onProgressUpdate(int progress) {
		return 0;
	}
	
	@Override
	protected void onPostExecute (Image result) {
		context.onAsyncTaskResult(this, result);
	}

}
