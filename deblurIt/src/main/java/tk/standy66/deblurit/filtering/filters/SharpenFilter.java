package tk.standy66.deblurit.filtering.filters;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import tk.standy66.deblurit.filtering.blur.Blur;
import tk.standy66.deblurit.tools.Image;
import tk.standy66.deblurit.tools.LibImageFilters;

public class SharpenFilter extends Filter {
	protected Blur b;
	protected double alpha;
	public SharpenFilter(Blur b, double alpha) {
		this.b = b;
		this.alpha = alpha;
	}
	
	@Override
	public Image apply(Image image) {
		Log.i("SharpenFilter", "here");
		int w = image.getWidth();
		int h = image.getHeight();
		b.setScaling(scaling);
		Image kernel = b.getKernel();
		Image original = image.clone();
		LibImageFilters.convolve(kernel.getChannel(0), image.getChannels(), w, h, kernel.getWidth(), kernel.getHeight());
		for (int i = 0; i < w; i++)
			for (int j = 0; j < h; j++)
				for (int k = 0; k < image.getChannelCount(); k++) {
					int bp = image.getPixel(i, j, k);
					int op = original.getPixel(i, j, k);
					double val = op + alpha * (op - bp);
					if (val > 255)
						val = 255;
					if (val < 0)
						val = 0;
					image.setPixel(i, j, k, (int)val);
				}
		return image;
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(b, flags);
		dest.writeDouble(alpha);
	}
	
	public SharpenFilter(Parcel in) {
		b = (Blur)in.readParcelable(Blur.class.getClassLoader());
		alpha = in.readDouble();
	}
	
	public static final Parcelable.Creator<SharpenFilter> CREATOR = new Creator<SharpenFilter>() {
		public SharpenFilter[] newArray(int size) {
			return new SharpenFilter[size];
		}
		public SharpenFilter createFromParcel(Parcel source) {
			return new SharpenFilter(source);
		}
	};
}
