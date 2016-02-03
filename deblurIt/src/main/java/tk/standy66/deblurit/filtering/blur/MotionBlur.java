package tk.standy66.deblurit.filtering.blur;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;

import tk.standy66.deblurit.tools.Image;

public class MotionBlur extends Blur {

    private float angle;
    private float length;
    private int width, height;
    public MotionBlur(float angle, float length) {
        this.angle = angle;
        this.length = length;
        width = height = (int)(Math.ceil(length) + 1);
    }

    public MotionBlur(Parcel in) {
        width = in.readInt();
        height = in.readInt();
        angle = in.readFloat();
        length = in.readFloat();
    }

    @Override
    public void setScaling(float value) {
        length /= value;
        if (length < 1)
            length = 1;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeFloat(angle);
        dest.writeFloat(length);
    }

    float cos(float angle) {
        return (float)Math.cos(angle);
    }

    float sin(float angle) {
        return (float)Math.sin(angle);
    }

    @Override
    public Image getKernel(int width, int height) {

        Bitmap b = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas c = new Canvas(b);

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.WHITE);
        float len2 = length / 2;
        float centerX = (float)width / 2;
        float centerY = (float)height / 2;

        float startX = centerX - len2 * cos(angle);
        float endX = centerX + len2 * cos(angle);

        float startY = centerY - len2 * sin(angle);
        float endY = centerY + len2 * sin(angle);
        c.drawLine(startX, startY, endX, endY, p);
        return Image.fromBitmap(b).toGrayscale();
    }

    @Override
    public int getRealWidth() {
        return width;
    }

    @Override
    public int getRealHeight() {
        return height;
    }

    public static final Parcelable.Creator<MotionBlur> CREATOR = new Creator<MotionBlur>() {
        public MotionBlur[] newArray(int size) {
            return new MotionBlur[size];
        }
        public MotionBlur createFromParcel(Parcel source) {
            return new MotionBlur(source);
        }
    };

}
