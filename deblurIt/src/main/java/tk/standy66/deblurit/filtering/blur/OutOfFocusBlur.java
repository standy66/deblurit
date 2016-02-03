package tk.standy66.deblurit.filtering.blur;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import tk.standy66.deblurit.tools.Image;

public class OutOfFocusBlur extends Blur {

    private float radius;

    public OutOfFocusBlur(float radius) {
        this.radius = radius;
    }

    @Override
    public void setScaling(float value) {
        Log.i("Outoffocusblur", "Radius was " + String.valueOf(radius));
        radius /= value;
        Log.i("Outoffocusblur", "Radius is " + String.valueOf(radius));
        if (radius < 1)
            radius = 1;
    }

    @Override
    public Image getKernel(int width, int height) {
        Bitmap b = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.WHITE);
        float centerX = (float)width / 2;
        float centerY = (float)height / 2;
        c.drawCircle(centerX, centerY, radius, p);
        return Image.fromBitmap(b).toGrayscale();
    }

    @Override
    public int getRealWidth() {
        return (int)(2 * radius + 1);
    }

    @Override
    public int getRealHeight() {
        return (int)(2 * radius + 1);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(radius);
    }

    public OutOfFocusBlur(Parcel in) {
        radius = (float) in.readDouble();
    }

    public static final Parcelable.Creator<OutOfFocusBlur> CREATOR = new Creator<OutOfFocusBlur>() {
        public OutOfFocusBlur[] newArray(int size) {
            return new OutOfFocusBlur[size];
        }
        public OutOfFocusBlur createFromParcel(Parcel source) {
            return new OutOfFocusBlur(source);
        }
    };

}
