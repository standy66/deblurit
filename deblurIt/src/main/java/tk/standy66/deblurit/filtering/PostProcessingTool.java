package tk.standy66.deblurit.filtering;

import tk.standy66.deblurit.tools.Image;
import android.graphics.Rect;
import android.util.Log;

public class PostProcessingTool implements Runnable {

	private Image image;
	private ProcessingContext context;
	private int originalWidth;
	private int originalHeight;
	
	public PostProcessingTool(Image image, ProcessingContext context, int originalWidth, int originalHeight) {
		this.image = image;
		this.context = context;
		this.originalHeight = originalHeight;
		this.originalWidth = originalWidth;
	}
	
	private void crop() {
		Rect clippingRectangle = context.getClippingRectangle();
		int x0 = clippingRectangle.left;
		int y0 = clippingRectangle.top;
		int x = originalWidth - clippingRectangle.right;
		int y = originalHeight - clippingRectangle.bottom;
		Image temp = new Image(image.getType(), x - x0, y - y0);
		for (int i = x0; i < x; i++)
			for (int j = y0; j < y; j++) {
				temp.fastSetPixel(i - x0, j - y0, image.fastGetPixel(i, j));
			}
		image = temp;
	}
	
	public void run() {
		/*
		Log.i("Post-processing", String.format("%d %d", originalWidth, originalHeight));
		Log.i("Post-processing", String.format("%d %d", image.getWidth(), image.getHeight()));
		
		if (!context.getClippingRectangle().equals(new Rect(0, 0, 0, 0)) || originalWidth != image.getWidth() || originalHeight != image.getHeight()) {
			long time = System.currentTimeMillis();
			crop();
			Log.i("Pre-Processing", String.format("Final cropping time: %f ms", (float)(System.currentTimeMillis() - time)));
		}*/
	}
	
	public Image getImage() {
		return image;
	}

}
