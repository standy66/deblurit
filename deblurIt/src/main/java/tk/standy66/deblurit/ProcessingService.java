package tk.standy66.deblurit;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcelable;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import tk.standy66.deblurit.filtering.Pipeline;
import tk.standy66.deblurit.tools.App;
import tk.standy66.deblurit.tools.GlobalSettings;
import tk.standy66.deblurit.tools.LibImageFilters;

public class ProcessingService extends IntentService {
	
	public interface ProcessingServiceResultListener {
		public void onResult(String result);
	}
	
	private NotificationManager notificationManager;
	private Notification notification;
	private int curProgress = 0;
	private int maxProgress = 0;
	private long lastMills = 0;
	private int lastProgress = 0;
	private float speed = 0;
	private final ProcessingServiceBinder binder = new ProcessingServiceBinder();
	private GlobalSettings gs = new GlobalSettings();
	
	public class ProcessingServiceBinder extends Binder {
		public float getTimeRemaining() {
			//Log.i("ProcessingService", String.format("%d %f", curProgress, speed));
			return (float)curProgress / speed;
		}
		
		private ProcessingServiceResultListener listener;
		
		public void setOnResultListener(ProcessingServiceResultListener listener) {
			this.listener = listener;
		}
		
	}
	
	public ProcessingService() {
		super("ProcessingService");
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		LibImageFilters.registerSubscriber(ProcessingService.class, this, "onProgressUpdate", "(I)I");
	}
	
	private String saveImage(Bitmap b) {
		try {
			String state = Environment.getExternalStorageState();
	
			if (Environment.MEDIA_MOUNTED.equals(state)) {
				try {
					String path = gs.getSavePath();
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
					Date now = new Date();
					Log.i("ProcessingService", "Selected format is " + gs.getFormat());
					String fileName = formatter.format(now) + (gs.getFormat().equals("JPEG") ? ".jpg" : ".png");
					File file = new File(path);
					file.mkdirs();
					file = new File(file, fileName);
					file.createNewFile();
					FileOutputStream fOut = new FileOutputStream(file);
					
					b.compress((gs.getFormat().equals("JPEG") ? CompressFormat.JPEG : CompressFormat.PNG), 100, fOut);
					
					fOut.flush();
					fOut.close();
					Log.i("ProcessingService", file.getAbsolutePath());
			
					return file.getAbsolutePath();
				} catch (IOException e) {
					Toast.makeText(this, R.string.toast_error_creating_file, Toast.LENGTH_LONG).show();
					Log.e("ProcessingService", e.getMessage());
					return null;
				}
			} else
				return null;
		} finally {
			b.recycle();
		}
	}

	private boolean handling = false;
	private boolean ready = false;
	private boolean cancelled = false;
	
	@Override
	protected void onHandleIntent(Intent i) {
		App.setApplicationContext(getApplicationContext());
		Parcelable parcel = i.getParcelableExtra("pipeline");
		if (parcel == null || handling)
			return;
		ready = false;
		handling = true;
		cancelled = false;
		createNotif();
		Pipeline p = (Pipeline)parcel;
		p.run();
		if (cancelled)
			return;
		String s = saveImage(p.getImage().toBitmap());
		binder.listener.onResult(s);
		Log.i("ProcessingService", s);
		sendReadyNotif(s);
		handling = false;
		ready = true;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	public int onProgressUpdate(int progress) {
		if (maxProgress == 0)
			maxProgress = progress;
		if (lastMills == 0 && lastProgress == 0) {
			lastMills = System.currentTimeMillis();
			lastProgress = progress;
		}
		
		long dt = System.currentTimeMillis() - lastMills;
		long dp = maxProgress - progress;
		speed = (float)dp / (float)dt * 1000;
	
		curProgress = progress;
		notification.contentView.setProgressBar(R.id.notification_progressbar, maxProgress, maxProgress - curProgress, false);
	    notification.contentView.setTextViewText(R.id.notification_pb_value, String.format("%d%%", (int)(100 * (float)(maxProgress - curProgress) / maxProgress)));
	    notificationManager.notify(0, notification);
		if (cancelled) {
		    Log.i("ProcessingService", "Send cancelled");
			return 1;
		} else
			return 0;
	}
	
	void createNotif() {
	    Intent intent = new Intent(this, ProgressActivity.class);
	    PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
	    
	    notification = new Notification(R.drawable.ic_launcher, "Processing", System.currentTimeMillis());
	    notification.contentIntent = pIntent;
	    notification.contentView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.notification_progress);
	    notification.flags |= Notification.FLAG_ONGOING_EVENT;
	    notification.contentView.setProgressBar(R.id.notification_progressbar, 100, 1, false);
	    notification.contentView.setTextViewText(R.id.notification_pb_value, "0%");
	    java.text.DateFormat df = DateFormat.getTimeFormat(App.getApplicationContext());
	    String time = df.format(new Date());
	    notification.contentView.setTextViewText(R.id.text_timestamp, time);
	    	    
	    notificationManager.notify(0, notification);
	}
	
	private static int pendingIntentVersion = 0;
	void sendReadyNotif(String imageUrl) {
	    Intent intent = new Intent(this, FinishActivity.class);
	    intent.putExtra(FinishActivity.IMAGE_URI, imageUrl);
	    PendingIntent pIntent = PendingIntent.getActivity(this, pendingIntentVersion++, intent, 0);
	    
	    notification.contentView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.notification_ready);
	    notification.flags &= ~Notification.FLAG_ONGOING_EVENT;
	    notification.contentIntent = pIntent;
	    java.text.DateFormat df = DateFormat.getTimeFormat(App.getApplicationContext());
	    String time = df.format(new Date());
	    notification.contentView.setTextViewText(R.id.text_timestamp, time);	    
	    
	    notificationManager.notify(0, notification);
	}
	
	@Override
	public void onDestroy() {
		cancelled = true;
		LibImageFilters.removeSubscriber();
		if (!ready)
			notificationManager.cancel(0);
		super.onDestroy();
	}
}
