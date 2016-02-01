package tk.standy66.deblurit.filtering.blur;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.FloatMath;
import tk.standy66.deblurit.filtering.filters.WienerFilter;
import tk.standy66.deblurit.tools.Image;
import tk.standy66.deblurit.tools.Image.ImageType;
import tk.standy66.deblurit.tools.Utils;

public class GaussianBlur extends Blur {

	private double radius;
	
	public GaussianBlur(double radius) {
		this.radius = radius;
	}

	@Override
	public void setScaling(float value) {
		radius /= value;
		if (radius < 1)
			radius = 1;
	}
	
	@Override
	public Image getKernel(int width, int height) {
		Image kernel = new Image(ImageType.GRAYSCALE, width, height);
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++) {
				int val = (int)(255 * Math.exp(-(Utils.sqr(x - 4 * radius) + Utils.sqr(y - 4 * radius)) / (2 * Utils.sqr(radius))));
				kernel.fastSetPixel(x, y, val);
			}
		return kernel;	
	}

	@Override
	public int getRealWidth() {
		return (int)(4 * 2 * radius + 3);
	}

	@Override
	public int getRealHeight() {
		return (int)(4 * 2 * radius + 3);
	}
	
	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeDouble(radius);
	}
	
	public GaussianBlur(Parcel in) {
		radius = in.readDouble();
	}
	
	public static final Parcelable.Creator<GaussianBlur> CREATOR = new Creator<GaussianBlur>() {
		public GaussianBlur[] newArray(int size) {
			return new GaussianBlur[size];
		}
		public GaussianBlur createFromParcel(Parcel source) {
			return new GaussianBlur(source);
		}
	};

}
