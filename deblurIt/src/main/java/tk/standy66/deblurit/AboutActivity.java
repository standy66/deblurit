package tk.standy66.deblurit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import tk.standy66.deblurit.tools.Utils;

public class AboutActivity extends AppCompatActivity {

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

    @Override
    protected void onResume() {
        super.onResume();
        Utils.analyticsLogScreenChange(getApplication(), "About");
    }
}
