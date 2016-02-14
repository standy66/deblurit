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
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import tk.standy66.deblurit.filtering.Pipeline;
import tk.standy66.deblurit.tools.App;
import tk.standy66.deblurit.tools.CapturePhotoUtils;
import tk.standy66.deblurit.tools.GlobalSettings;
import tk.standy66.deblurit.tools.LibImageFilters;

public class ProcessingService extends IntentService {

    public interface ProcessingServiceResultListener {
        void onResult(String result);
    }

    private NotificationManager notificationManager;
    private int curProgress = 0;
    private int maxProgress = 0;
    private long lastMills = 0;
    private int lastProgress = 0;
    private float speed = 0;
    private final ProcessingServiceBinder binder = new ProcessingServiceBinder();
    private GlobalSettings gs = new GlobalSettings();

    public class ProcessingServiceBinder extends Binder {
        public float getTimeRemaining() {
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
        FileOutputStream fos = null;
        File f = null;
        try {
            f = new File(getFilesDir(), "result" + (gs.getFormat().equals("JPEG") ? ".jpg" : ".png"));
            fos = new FileOutputStream(f);
            b.compress(gs.getFormat().equals("JPEG") ? CompressFormat.JPEG : CompressFormat.PNG, 90, fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return f.getAbsolutePath();
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
        Bitmap bmp = p.getImage().toBitmap();
        String s = saveImage(bmp);
        binder.listener.onResult(s);
        Log.i("ProcessingService", s);
        sendReadyNotif(s, bmp);
        handling = false;
        ready = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    //called from native code
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

        Notification notification = getProgressNotificationBuilder()
                .setContentText(getString(R.string.notification_processing_text) +
                        String.format("%d%%", (int)(100 * (float)(maxProgress - curProgress) / maxProgress)))
                .setProgress(maxProgress, maxProgress - curProgress, false)
                .build();
        notificationManager.notify(0, notification);
        if (cancelled) {
            Log.i("ProcessingService", "Send cancelled");
            return 1;
        } else
            return 0;
    }

    void createNotif() {
        Notification notification = getProgressNotificationBuilder()
                .setProgress(100, 0, false)
                .build();

        notificationManager.notify(0, notification);
    }

    void sendReadyNotif(String imageUrl, Bitmap bitmap) {
        Notification notification = getReadyNotificationBuilder(imageUrl, bitmap).build();
        notificationManager.notify(0, notification);
    }

    private NotificationCompat.Builder getReadyNotificationBuilder(String imageUrl, Bitmap bitmap) {
        Intent resultIntent = new Intent(this, FinishActivity.class);
        resultIntent.putExtra(FinishActivity.IMAGE_URI, imageUrl);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(ProgressActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle();
        style.bigPicture(bitmap);
        style.setSummaryText(getString(R.string.notification_ready_text));

        return new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.notification_ready_label))
                .setContentText(getString(R.string.notification_ready_text))
                .setContentIntent(resultPendingIntent)
                .setSmallIcon(R.drawable.ic_camera_white_24dp)
                .setStyle(style);
    }

    private NotificationCompat.Builder getProgressNotificationBuilder() {
        Intent resultIntent = new Intent(this, ProgressActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(ProgressActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        return new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.notification_processing_label))
                .setContentText(getString(R.string.notification_processing_text) + "0%")
                .setContentIntent(resultPendingIntent)
                .setSmallIcon(R.drawable.ic_camera_white_24dp);
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
