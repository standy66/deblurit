package tk.standy66.deblurit;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

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
        sendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(FeedbackActivity.this, "Not implemented", Toast.LENGTH_LONG).show();
            }
        });
    }   
}
