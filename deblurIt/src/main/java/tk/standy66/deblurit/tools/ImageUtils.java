package tk.standy66.deblurit.tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

import tk.standy66.deblurit.R;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public final class ImageUtils {
	public static Bitmap decodeFileScaled(Uri uri, int maxDimen, boolean strict, MutableDouble outScaling) throws IOException {
		try {
			InputStream is = App.getApplicationContext().getContentResolver().openInputStream(uri);
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;		
			BitmapFactory.decodeStream(is, null, opts);
			int scaling = 1;
			int w = opts.outWidth;
			int h = opts.outHeight;
			Log.i("ImageUtils", String.format("Image sizes are: %d %d", w, h));		
			Log.i("ImageUtils", String.format("MaxDimen is %d", maxDimen));		
			while ((w / scaling) > maxDimen || (h / scaling) > maxDimen)
				scaling++;
			if (strict)
				scaling--;		
			Log.i("ImageUtils", String.format("Desired scaling is %d", scaling));
			opts.inJustDecodeBounds = false;
			opts.inSampleSize = scaling;
			is.close();
			is = App.getApplicationContext().getContentResolver().openInputStream(uri);
			Bitmap temp = BitmapFactory.decodeStream(is, null, opts);
			is.close();
			outScaling.value = ((double)w / opts.outWidth);
			w = opts.outWidth;
			h = opts.outHeight;
			Log.i("ImageUtils", String.format("Resulted scaling is %f", outScaling.value));
			Log.i("ImageUtils", String.format("Resulted sizes are %d %d", w, h));
			
			if (strict) {
				Log.i("ImageUtils", "Strict mode");
				double scaling2 = (double)Math.max(w, h) / maxDimen;
				Log.i("ImageUtils", String.format("Scaling2 is %f", scaling2));
				if (scaling2 <= 1.0)
					return temp;
				else {
					w = (int)(w / scaling2);
					h = (int)(h / scaling2);
					Bitmap temp2 = Bitmap.createScaledBitmap(temp, w, h, true);
					outScaling.value *= scaling2;
					temp.recycle();
					Log.i("ImageUtils", String.format("Strict resuls are %d %d", temp2.getWidth(), temp2.getHeight()));
					return temp2;
				}
			} else 
				return temp;
		} catch (OutOfMemoryError e) {
			Toast.makeText(App.getApplicationContext(), App.getApplicationContext().getResources().getString(R.string.toast_out_of_memory), Toast.LENGTH_LONG).show();
			System.exit(0);
			return null;			
		}
		
	}

}
