package tk.standy66.deblurit.tools;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
            File outputDir = context.getCacheDir(); // context being the Activity pointer
            File outputFile = File.createTempFile("file", "jpg", outputDir);
            FileOutputStream fos = new FileOutputStream(outputFile);
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            is.close();
            fos.close();
            return outputFile.toString();
        } catch (IOException e) {
            return null;
        }

        /*String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = App.getApplicationContext().getContentResolver().query(contentUri, proj, null, null, null);
        Log.i("Utils", contentUri.toString());
        if (cursor == null)
            return contentUri.toString();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        String result;
        if (cursor.moveToNext()) {
            result = cursor.getString(column_index);
        } else {
            result = null;
        }
        cursor.close();
        return result;*/
    }

    public static float getMaxMemory() {
        return Runtime.getRuntime().maxMemory() / 1048576.0f;
    }
}
