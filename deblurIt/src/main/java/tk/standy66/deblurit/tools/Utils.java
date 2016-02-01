package tk.standy66.deblurit.tools;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public final class Utils {
	public static float sqr(float x) {
		return x * x;
	}	
	public static double sqr(double x) {
		return x * x;
	}
	public static String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = App.getApplicationContext().getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor == null)
        	return contentUri.toString();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

	public static float getMaxMemory() {
		return Runtime.getRuntime().maxMemory() / 1048576.0f;
	}
}
