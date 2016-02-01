package tk.standy66.deblurit.filtering.filters;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import tk.standy66.deblurit.filtering.blur.Blur;
import tk.standy66.deblurit.tools.Image;
import tk.standy66.deblurit.tools.LibImageFilters;

public class ConvolutionFilter extends Filter {

	private Blur blur;
	public ConvolutionFilter(Blur blur) {
		this.blur = blur;
	}
	
	@Override
	public Image apply(Image image) {
		int w = image.getWidth();
		int h = image.getHeight();
		blur.setScaling(scaling);
		Image kernel = blur.getKernel();
		LibImageFilters.convolve(kernel.getChannel(0), image.getChannels(), w, h, kernel.getWidth(), kernel.getHeight());
		return image;
	}
	
	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(blur, flags);
	}
	
	public ConvolutionFilter(Parcel in) {
		blur = in.readParcelable(ConvolutionFilter.class.getClassLoader());
	}
	
	public static final Parcelable.Creator<ConvolutionFilter> CREATOR = new Creator<ConvolutionFilter>() {
		public ConvolutionFilter[] newArray(int size) {
			return new ConvolutionFilter[size];
		}
		public ConvolutionFilter createFromParcel(Parcel source) {
			return new ConvolutionFilter(source);
		}
	};
}
