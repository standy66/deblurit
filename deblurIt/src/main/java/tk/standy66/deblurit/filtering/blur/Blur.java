package tk.standy66.deblurit.filtering.blur;

import android.os.Parcelable;
import tk.standy66.deblurit.tools.Image;

public abstract class Blur implements Parcelable {
	public abstract Image getKernel(int width, int height);	
	public abstract int getRealWidth();
	public abstract int getRealHeight();
	
	public abstract void setScaling(float value);

	public Image getKernel() {
		return getKernel(getRealWidth(), getRealHeight());
	}
}
