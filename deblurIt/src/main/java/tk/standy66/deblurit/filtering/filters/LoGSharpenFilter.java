package tk.standy66.deblurit.filtering.filters;

import android.os.Parcel;
import android.os.Parcelable;

import tk.standy66.deblurit.tools.Image;
import tk.standy66.deblurit.tools.LibImageFilters;

public class LoGSharpenFilter extends Filter {
    protected double radius;
    protected double alpha;

    public LoGSharpenFilter(double radius, double alpha) {
        this.radius = radius;
        this.alpha = alpha;
    }

    @Override
    public Image apply(Image image) {
        int w = image.getWidth();
        int h = image.getHeight();
        LibImageFilters.filterLoG(image.getChannels(), w, h, (float)radius, (float)alpha);
        return image;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(radius);
        dest.writeDouble(alpha);
    }

    public LoGSharpenFilter(Parcel in) {
        radius = in.readDouble();
        alpha = in.readDouble();
    }

    public static final Parcelable.Creator<LoGSharpenFilter> CREATOR = new Creator<LoGSharpenFilter>() {
        public LoGSharpenFilter[] newArray(int size) {
            return new LoGSharpenFilter[size];
        }
        public LoGSharpenFilter createFromParcel(Parcel source) {
            return new LoGSharpenFilter(source);
        }
    };
}
