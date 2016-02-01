package tk.standy66.deblurit;

import org.acra.ACRA;

import tk.standy66.deblurit.R;
import tk.standy66.deblurit.tools.GMailSender;

import com.actionbarsherlock.app.SherlockActivity;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class FeedbackActivity extends Activity {

	Button sendButton, discardButton;
	EditText name, email, message;
	Spinner feedbackType;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        sendButton = (Button)findViewById(R.id.button_send);
        discardButton = (Button)findViewById(R.id.button_discard);
        name = (EditText)findViewById(R.id.field_name);
        email = (EditText)findViewById(R.id.field_email);
        message = (EditText)findViewById(R.id.field_message);
        feedbackType = (Spinner)findViewById(R.id.spinner_bugtype);
        
        
        discardButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				finish();
				
			}
		});
        
        sendButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Log.i("FeedbackActivity", "here");
				String subject = "[DeblurIt]New feedback from " + name.getText().toString() + " (" + email.getText().toString() + ")";
				String body = (String)feedbackType.getSelectedItem() + "\n" + message.getText().toString();
				String sender = email.getText().toString();
				String recipient = "karn9050@gmail.com";
				new MailSenderAsyncTask().execute(subject, body, sender, recipient);
				Toast.makeText(FeedbackActivity.this, R.string.feedback_send_toast, Toast.LENGTH_LONG).show();
				finish();
				
			}
		});
    }   
}
