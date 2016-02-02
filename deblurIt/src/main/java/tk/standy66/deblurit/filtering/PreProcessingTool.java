package tk.standy66.deblurit.filtering;

import android.graphics.Rect;
import android.util.Log;

import tk.standy66.deblurit.tools.Image;
import tk.standy66.deblurit.tools.Image.ImageType;

public class PreProcessingTool implements Runnable {

	private Image image;
	private ProcessingContext context;
	private int originalWidth, originalHeight;
	
	public PreProcessingTool(Image image, ProcessingContext context) {
		this.image = image;
		this.context = context;
	}

	public Image getImage() {
		return image;
	}

	private void clipImage() {
		Rect clippingRectangle = context.getProcessingRectangle();
		Log.i("Pre-Processing", clippingRectangle.toShortString());
		int w = image.getWidth();
		int h = image.getHeight();
		int x0 = clippingRectangle.left;
		int y0 = clippingRectangle.top;
		int x = w - clippingRectangle.right;
		int y = h - clippingRectangle.bottom;
		Image temp = new Image(image.getType(), x - x0, y - y0);
		for (int i = x0; i < x; i++)
			for (int j = y0; j < y; j++) {
				int pixel = image.fastGetPixel(i, j);
				temp.fastSetPixel(i - x0, j - y0, pixel);
			}
		image = temp;
		
	}
	
	private int unsignedToBytes(byte b) {
		return b & 0xFF;
	}
	
	private void turnGrayscale() {
		if (image.getType() != ImageType.RGB)
			throw new RuntimeException("Image must be RGB type");
		image = image.toGrayscale();
	}
	
	public int getOriginalWidth() {
		return originalWidth;
	}
	
	public int getOriginalHeight() {
		return originalHeight;
	}
	
	private void completeImage() {
		int w = image.getWidth();
		int h = image.getHeight();
		originalWidth = w;
		originalHeight = h;
		int lg_w = 0;
		int lg_h = 0;
		while ((1 << lg_w) < w)
			lg_w++;
		while ((1 << lg_h) < h)
			lg_h++;
		if (1 << lg_w == w && 1 << lg_h == h)
			return;
		lg_w = lg_h = Math.max(lg_w, lg_h);
		Image temp = new Image(image.getType(), 1 << lg_w, 1 << lg_h);
		for (int i = 0; i < image.getWidth(); i++)
			for (int j = 0; j < image.getHeight(); j++)
				temp.fastSetPixel(i, j, image.fastGetPixel(i, j));
		image = temp;
	}
	
	public void run() {
		long time;
		if (context.isTurnGrayscale()) {
			time = System.currentTimeMillis();
			turnGrayscale();
			Log.i("Pre-Processing", String.format("Turning grayscale time: %f ms", (float)(System.currentTimeMillis() - time)));
		}
		if (!context.getProcessingRectangle().equals(new Rect(0, 0, 0, 0))) {
			time = System.currentTimeMillis();
			clipImage();
			Log.i("Pre-Processing", String.format("Image clipping time: %f ms", (float)(System.currentTimeMillis() - time)));
		}
		/*time = System.currentTimeMillis();
		completeImage();
		Log.i("Pre-Processing", String.format("Pow-of-2 compliting time: %f ms", (float)(System.currentTimeMillis() - time)));*/
	}

}
