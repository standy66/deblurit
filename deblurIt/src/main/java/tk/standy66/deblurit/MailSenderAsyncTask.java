package tk.standy66.deblurit;

import tk.standy66.deblurit.filtering.Pipeline;
import tk.standy66.deblurit.tools.App;
import tk.standy66.deblurit.tools.GMailSender;
import tk.standy66.deblurit.tools.Image;
import tk.standy66.deblurit.tools.LibImageFilters;
import android.os.AsyncTask;
import android.provider.VoicemailContract;
import android.util.Log;
import android.widget.Toast;

public class MailSenderAsyncTask extends AsyncTask<Object, Integer, Integer> {

	public MailSenderAsyncTask() {
	}
	@Override
	protected Integer doInBackground(Object... arg0) {
		String subject = (String)arg0[0];
		String body = (String)arg0[1];
		String mailSender = "automailing66@gmail.com";
		String recipients = (String)arg0[3];
		Log.i("MailSenderAsyncTask", String.format("Here got: %s %s %s %s", subject, body, mailSender, recipients));
		GMailSender sender = new GMailSender("automailing66@gmail.com", "whenyouseeityouwillshitbrix");
		try {
			sender.sendMail(subject, body, mailSender, recipients);
			Log.i("MailSenderAsyncTask", "Sending...");
		} catch (Exception e) {
			Log.i("MailSenderAsyncTask", "There was a exception");
			
		}
		return 0;
	}
}
