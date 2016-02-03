package tk.standy66.deblurit;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import tk.standy66.deblurit.ProcessingService.ProcessingServiceBinder;
import tk.standy66.deblurit.ProcessingService.ProcessingServiceResultListener;

public class ProgressActivity extends AppCompatActivity implements ProcessingServiceResultListener {
	

	private ProcessingServiceBinder binder;
	private boolean mBound = false;
	private Timer t = new Timer();
	private TextView progressTextView;
	
	private class ProgressUpdateTask extends TimerTask {
		@Override
		public void run() {
			if (binder == null)
				return;
			runOnUiThread(new Runnable() {
				public void run() {
					float timeRemaining = binder.getTimeRemaining();
					if (progressTextView != null)
						progressTextView.setText(String.format(getResources().getString(R.string.progress_time_remaining), (int)timeRemaining));
				}
			});
		}
		
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
        progressTextView = (TextView)findViewById(R.id.progress_textview);
        
    }

    @Override
    protected void onStart() {
    	super.onStart();
    	Intent intent = new Intent(this, ProcessingService.class);
    	bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	if (mBound) {
    		unbindService(mConnection);
    		mBound = false;
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.activity_progress, menu);
        return true;
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                IBinder service) {
            binder = (ProcessingServiceBinder) service;
            binder.setOnResultListener(ProgressActivity.this);
            mBound = true;
            t.schedule(new ProgressUpdateTask(), 0, 1000);
        }

        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private String result;
    private boolean visible;
    
    @Override
    protected void onResume() {
    	visible = true;
    	super.onResume();
    }
    
    @Override
    protected void onPause() {
    	visible = false;
    	super.onPause();
    }
    
	public void onResult(final String result) {
		runOnUiThread(new Runnable() {
			public void run() {
				if (visible) {
					Intent i = new Intent(ProgressActivity.this, FinishActivity.class);
					i.putExtra(FinishActivity.IMAGE_URI, result);
					startActivity(i);
				}
				finish();
			}
		});
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.menu_close:
        	Intent intent = new Intent(this, ProcessingService.class);
    		unbindService(mConnection);
    		mBound = false;
    		stopService(intent);
    		finish();
    		break;
		}
    	return super.onOptionsItemSelected(item);
    }
}
