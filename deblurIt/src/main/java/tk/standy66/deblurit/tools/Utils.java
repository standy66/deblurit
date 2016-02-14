package tk.standy66.deblurit.tools;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import tk.standy66.deblurit.DeblurItApplication;

public final class Utils {
    public static float sqr(float x) {
        return x * x;
    }
    public static double sqr(double x) {
        return x * x;
    }
    public static String getRealPathFromURI(Context context, Uri contentUri) {
        ContentResolver resolver = context.getContentResolver();
        InputStream is = null;
        try {
            is = resolver.openInputStream(contentUri);
            Bitmap bmp = BitmapFactory.decodeStream(is);
            File outputDir = context.getCacheDir();
            File outputFile = File.createTempFile("file", "jpg", outputDir);
            FileOutputStream fos = new FileOutputStream(outputFile);
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            is.close();
            fos.close();
            return outputFile.toString();
        } catch (IOException e) {
            return null;
        }
    }

    public static void analyticsLogScreenChange(Application application, String screenName) {
        DeblurItApplication a = (DeblurItApplication) application;
        Tracker t = a.getDefaultTracker();

        Log.i("Analytics", "Setting screen name: " + screenName);
        t.setScreenName("Image~" + screenName);
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public static float getMaxMemory() {
        return Runtime.getRuntime().maxMemory() / 1048576.0f;
    }
}
