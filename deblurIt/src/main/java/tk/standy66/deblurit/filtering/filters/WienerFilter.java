package tk.standy66.deblurit.filtering.filters;

import android.os.Parcel;
import android.os.Parcelable;

import tk.standy66.deblurit.filtering.blur.Blur;
import tk.standy66.deblurit.tools.Image;
import tk.standy66.deblurit.tools.LibImageFilters;

public class WienerFilter extends Filter {
	
	protected Blur blur;
	protected float lambda;
	
	public WienerFilter(Blur blur, float lambda) {
		this.blur = blur;
		this.lambda = lambda;
	}
	
	@Override
	public Image apply(Image image) {
		int w = image.getWidth();
		int h = image.getHeight();
		blur.setScaling(scaling);
		Image kernel = blur.getKernel();
		LibImageFilters.deconvolve(kernel.getChannel(0), image.getChannels(), w, h, lambda, kernel.getWidth(), kernel.getHeight());
		return image;
	}
	
	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeDouble(lambda);
		dest.writeParcelable(blur, flags);
	}
	
	public WienerFilter(Parcel in) {
		lambda = (float)in.readDouble();
		blur = (Blur)in.readParcelable(Blur.class.getClassLoader());
	}
	
	public static final Parcelable.Creator<WienerFilter> CREATOR = new Creator<WienerFilter>() {
		public WienerFilter[] newArray(int size) {
			return new WienerFilter[size];
		}
		public WienerFilter createFromParcel(Parcel source) {
			return new WienerFilter(source);
		}
	};
}
