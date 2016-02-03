package tk.standy66.deblurit.filtering;

import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class ProcessingContext implements Parcelable {
    private boolean turnGrayscale;
    private Rect processingRectangle;
    private Rect clippingRectangle;

    public ProcessingContext(Parcel in) {
        Log.i("ProcessingContext", "in ProcessingContext(Parcel)");
        processingRectangle = (Rect)in.readParcelable(Rect.class.getClassLoader());
        clippingRectangle = (Rect)in.readParcelable(Rect.class.getClassLoader());
        turnGrayscale = in.readByte() == 1;
    }

    public final static Parcelable.Creator<ProcessingContext> CREATOR = new Creator<ProcessingContext>() {

        public ProcessingContext[] newArray(int size) {
            Log.i("ProcessingContext", "in Creator.newArray(int)");
            return new ProcessingContext[size];
        }

        public ProcessingContext createFromParcel(Parcel source) {
            Log.i("ProcessingContext", "in Creator.createFromParcel(Parcel)");
            return new ProcessingContext(source);
        }
    };

    public ProcessingContext(boolean turnGrayscale) {
        this.turnGrayscale = turnGrayscale;
        this.processingRectangle = new Rect(0, 0, 0, 0);
        this.clippingRectangle = new Rect(0, 0, 0, 0);
    }

    public ProcessingContext(boolean turnGrayscale, Rect processingRectangle) {
        this.turnGrayscale = turnGrayscale;
        this.processingRectangle = processingRectangle;
        this.clippingRectangle = new Rect(0, 0, 0, 0);
    }

    public ProcessingContext(boolean turnGrayscale, Rect processingRectangle, Rect clippingRectangle) {
        this.turnGrayscale = turnGrayscale;
        this.processingRectangle = processingRectangle;
        this.clippingRectangle = clippingRectangle;
    }

    public Rect getProcessingRectangle() {
        return processingRectangle;
    }

    public boolean isTurnGrayscale() {
        return turnGrayscale;
    }

    public Rect getClippingRectangle() {
        return clippingRectangle;
    }

    public int describeContents() {
        Log.i("ProcessingContext", "in describeContents()");
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        Log.i("ProcessingContext", "in writeParcel()");
        out.writeParcelable(processingRectangle, flags);
        out.writeParcelable(clippingRectangle, flags);
        out.writeByte((byte) (turnGrayscale ? 1 : 0));
    }
}
