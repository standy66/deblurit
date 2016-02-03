package tk.standy66.deblurit.filtering;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;

import tk.standy66.deblurit.filtering.filters.Filter;
import tk.standy66.deblurit.tools.App;
import tk.standy66.deblurit.tools.GlobalSettings;
import tk.standy66.deblurit.tools.Image;
import tk.standy66.deblurit.tools.ImageUtils;
import tk.standy66.deblurit.tools.MutableDouble;

public class Pipeline implements Parcelable {

    private Image image;
    private Filter filter;
    private String bitmapUri = null;
    private ProcessingContext processingContext;
    private Bitmap bitmap = null;
    private GlobalSettings globalSettings = new GlobalSettings();
    private int desiredWidth, desiredHeight;

    public Pipeline(String bitmapUri, Filter filter, ProcessingContext processingContext) {
        this.bitmapUri = bitmapUri;
        this.filter = filter;
        this.processingContext = processingContext;
        desiredWidth = Integer.MAX_VALUE;
        desiredHeight = Integer.MAX_VALUE;
    }

    public Pipeline(String bitmapUri, Filter filter, ProcessingContext processingContext, int desiredWidth, int desiredHeight) {
        this.bitmapUri = bitmapUri;
        this.filter = filter;
        this.processingContext = processingContext;
        this.desiredHeight = desiredHeight;
        this.desiredWidth = desiredWidth;
    }

    public Pipeline(Bitmap bitmap, Filter filter, ProcessingContext processingContext) {
        this.bitmap = bitmap;;
        this.filter = filter;
        this.processingContext = processingContext;
    }

    public Pipeline(Parcel in) {
        Log.i("Pipeline", "reading bitmapUri");
        bitmapUri = in.readString();
        Log.i("Pipeline", "reading filter");
        filter = (Filter)in.readParcelable(Filter.class.getClassLoader());
        Log.i("Pipeline", "reading context");
        processingContext = (ProcessingContext)in.readParcelable(ProcessingContext.class.getClassLoader());
        bitmap = in.readParcelable(Bitmap.class.getClassLoader());
        desiredWidth = in.readInt();
        desiredHeight = in.readInt();
    }

    public void run() {
        try {
            long time = 0;
            if (bitmap == null) {
                Log.i("pipeline", bitmapUri);
                if (App.getApplicationContext() == null)
                    throw new RuntimeException("Application Context must be set before calling this method");

                MutableDouble outScaling = new MutableDouble(1);
                Bitmap b = ImageUtils.decodeFileScaled(Uri.parse(bitmapUri), Math.min(Math.max(desiredWidth, desiredHeight), globalSettings.getMaxOutputSize()), true, outScaling);
                time = System.currentTimeMillis();
                image = Image.fromBitmap(b);
                b.recycle();
                filter.setScaling((float)outScaling.value);
            } else
                image = Image.fromBitmap(bitmap);
            PreProcessingTool preProcessingTool = new PreProcessingTool(image, processingContext);
            preProcessingTool.run();
            int originalWidth = preProcessingTool.getOriginalWidth();
            int originalHeight = preProcessingTool.getOriginalHeight();
            image = preProcessingTool.getImage();
            System.gc();
            image = filter.apply(image);
            PostProcessingTool postProcessingTool = new PostProcessingTool(image, processingContext, originalWidth, originalHeight);
            postProcessingTool.run();
            image = postProcessingTool.getImage();
            Log.i("pipeline", String.format("All time: %fms", (float)(System.currentTimeMillis() - time)));
        } catch (FileNotFoundException e) {
            // TODO: handle exception
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Image getImage() {
        return image;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        Log.i("Pipeline", "writing bitmapUri");
        out.writeString(bitmapUri);
        Log.i("Pipeline", "writing filter");
        out.writeParcelable(filter, flags);
        Log.i("Pipeline", "writing context");
        out.writeParcelable(processingContext, flags);
        Log.i("Pipeline", "out of writing parcel");
        out.writeParcelable(bitmap, flags);
        out.writeInt(desiredWidth);
        out.writeInt(desiredHeight);
    }

    public static final Parcelable.Creator<Pipeline> CREATOR = new Creator<Pipeline>() {
        public Pipeline[] newArray(int size) {
            return new Pipeline[size];
        }
        public Pipeline createFromParcel(Parcel source) {
            return new Pipeline(source);
        }
    };
}
