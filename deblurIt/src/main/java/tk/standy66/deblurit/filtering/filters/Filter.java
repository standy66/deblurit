package tk.standy66.deblurit.filtering.filters;

import android.os.Parcelable;
import tk.standy66.deblurit.tools.Image;

public abstract class Filter implements Parcelable {
	public abstract Image apply(Image image);
	protected float scaling;
	public void setScaling(float scaling) {
		this.scaling = scaling;
	}
}
