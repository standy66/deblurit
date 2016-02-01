package tk.standy66.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class ScrollablePreview extends LinearLayout {
	
	public enum ScrollMode {
		Enabled,
		Disabled
	}

	private int maxX, maxY;
	private ImageView child = null;
	private float downX, downY;
	private boolean processedMode = false;
	private int lastScrollX, lastScrollY;
	private Bitmap originalBitmap;
	private ScrollMode scrollMode;
	
	public ScrollMode getScrollMode() {
		return scrollMode;
	}

	public void setScrollMode(ScrollMode scrollMode) {
		this.scrollMode = scrollMode;
	}

	public ScrollablePreview(Context context) {
		super(context);
	}
	
	public ScrollablePreview(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public ScrollablePreview(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
	}
	
	public void setMaxX(int maxX) {
		this.maxX = maxX;
	}
	
	public void setMaxY(int maxY) {
		this.maxY = maxY;
	}
	
	public int getMaxX() {
		return maxX;
	}
	
	public int getMaxY() {
		return maxY;
	}
	
	public Rect getVisibleRectange() {
		if (child == null)
			child = getChild();
		Rect result = new Rect(getScrollX(), getScrollY(), maxX - getScrollX() - Math.min(getWidth(), child.getWidth()), maxY - getScrollY() - Math.min(getHeight(), child.getHeight()));
		return result;
	}
	
	private ImageView getChild() {
		ImageView result = null;
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View v = getChildAt(i);
			if (v instanceof ImageView) {
				result = (ImageView)v;
				break;
			}
		}
		if (result == null)
			throw new RuntimeException("LinearLayout's content is null");
		return result;
	}
	
	public void setProcessedBitmap(Bitmap b, Bitmap originalBitmap) {
		if (child == null)
			child = getChild();
		if (scrollMode == ScrollMode.Disabled) {
			child.setImageBitmap(b);
			return;
		}
		if (child != null) {
			processedMode = true;
			this.originalBitmap = originalBitmap;
			lastScrollX = getScrollX();
			lastScrollY = getScrollY();
			scrollTo(0, 0);
			child.setImageBitmap(b);
			child.getLayoutParams().width = b.getWidth();
			child.getLayoutParams().height = b.getHeight();
		}
			
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (scrollMode == ScrollMode.Disabled)
			return true;
		
		if (child == null) {
			child = getChild();
		}
		if (processedMode == true) {
			processedMode = false;
			child.setImageBitmap(originalBitmap);
			child.getLayoutParams().width = originalBitmap.getWidth();
			child.getLayoutParams().height = originalBitmap.getHeight();
			scrollTo(lastScrollX, lastScrollY);
		}
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downX = event.getX();
			downY = event.getY();
			break;

		case MotionEvent.ACTION_MOVE:
			float currentX = event.getX();
			float currentY = event.getY();
			
			int scrollByX = (int)(downX - currentX);
			int scrollByY = (int)(downY - currentY);
			
			if (getScrollX() + getWidth() + scrollByX >= maxX && scrollByX > 0)
				scrollByX = maxX - getScrollX() - getWidth();
			if (getScrollX() + scrollByX < 0 && scrollByX < 0)
				scrollByX = -getScrollX();
			
			if (getScrollY() + getHeight() + scrollByY >= maxY && scrollByY > 0)
				scrollByY = maxY - getScrollY() - getHeight();
			if (getScrollY() + scrollByY < 0 && scrollByY < 0)
				scrollByY = -getScrollY();
			
			scrollBy(scrollByX, scrollByY);
			downX = currentX;
			downY = currentY;
			
			break;
		}
		
		return true;
		//return super.onTouchEvent(event);
	}
	
	

}
