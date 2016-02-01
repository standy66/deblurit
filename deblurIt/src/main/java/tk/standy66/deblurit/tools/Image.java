package tk.standy66.deblurit.tools;


import java.nio.ByteBuffer;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;

public class Image {

	public enum ImageType {
		RGB,
		GRAYSCALE
	};

	private byte[][][] channels;
	private int channelCount;
	private ImageType type;
	private int width, height;
	
	public Image(ImageType type, int width, int height) {
		this.type = type;
		this.width = width;
		this.height = height;
		if (type == ImageType.GRAYSCALE)
			channelCount = 1;
		else
			channelCount = 3;
		channels = new byte[channelCount][width][height];
	}
	
	public boolean isGrayscale() {
		return type == ImageType.GRAYSCALE;
	}
	
	public ImageType getType() {
		return type;
	}
	
	public int getChannelCount() {
		return channelCount;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public static Image fromBitmap(Bitmap bmp) {
		int w = bmp.getWidth();
		int h = bmp.getHeight();
		Image result = new Image(ImageType.RGB, w, h);
		
		for (int i = 0; i < w; i++)
			for (int j = 0; j < h; j++) {
				int pixel = bmp.getPixel(i, j);			
				int b = pixel & 0xFF;
				int g = (pixel >> 8) & 0xFF;
				int r = (pixel >> 16) & 0xFF;
				result.channels[0][i][j] = (byte) r;
				result.channels[1][i][j] = (byte) g;
				result.channels[2][i][j] = (byte) b;
			}
		return result;
	}
	
	private int unsignedToBytes(byte b) {
		return b & 0xFF;
	}
	
	public Bitmap toBitmap() {
		boolean grayscale = isGrayscale();
		Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++) {
				int R, G, B;
				if (grayscale)
					R = G = B = unsignedToBytes(channels[0][i][j]);
				else {
					R = (unsignedToBytes(channels[0][i][j]));
					G = (unsignedToBytes(channels[1][i][j]));
					B = (unsignedToBytes(channels[2][i][j]));
				}
				bitmap.setPixel(i, j, Color.rgb(R, G, B));
			}
		return bitmap;
	}
	
	public byte[] getPixel(int x, int y) {
		if (x < 0 || x >= width)
			throw new IllegalArgumentException("X must be in interval [0, width)");
		if (y < 0 || y >= height)
			throw new IllegalArgumentException("Y must be in interval [0, height)");
		byte[] result = new byte[channelCount];
		for (int i = 0; i < channelCount; i++)
			result[i] = channels[i][x][y];
		return result;
	}
	
	public void setPixel(int x, int y, byte[] pixel) {
		if (x < 0 || x >= width)
			throw new IllegalArgumentException("X must be in interval [0, width)");
		if (y < 0 || y >= height)
			throw new IllegalArgumentException("Y must be in interval [0, height)");
		if (pixel.length != channelCount)
			throw new IllegalArgumentException("Pixel data length must be the same as channelCount");
		for (int i = 0; i < channelCount; i++)
			channels[i][x][y] = pixel[i];
	}
	
	public void setPixel(int x, int y, int channel, int value) {
		channels[channel][x][y] = (byte)value;
	}
	
	public int getPixel(int x, int y, int channel) {
		return unsignedToBytes(channels[channel][x][y]);
	}
	
	public int fastGetPixel(int x, int y) {
		if (type == ImageType.GRAYSCALE)
			return unsignedToBytes(channels[0][x][y]);
		else
			return Color.rgb(unsignedToBytes(channels[0][x][y]), unsignedToBytes(channels[1][x][y]), unsignedToBytes(channels[2][x][y]));
	}
	
	public void fastSetPixel(int x, int y, int value) {
		if (type != ImageType.GRAYSCALE) {
			channels[0][x][y] = (byte) ((value >> 16) & 0xFF);
			channels[1][x][y] = (byte) ((value >> 8) & 0xFF);
			channels[2][x][y] = (byte) ((value) & 0xFF);
		}
		else
			channels[0][x][y] = (byte)value;
	}
	
	public void fastSetPixel(int x, int y, int r, int g, int b) {
		if (type != ImageType.RGB)
			throw new RuntimeException("Image is not RGB");
		channels[0][x][y] = (byte)r;
		channels[1][x][y] = (byte)g;
		channels[2][x][y] = (byte)b;
	}
	
	public byte[][] getChannel(int channelId) {
		if (channelId < 0 || channelId >= channelCount)
			throw new IllegalArgumentException("channelId must be in interval [0, channelCount)");
		return channels[channelId];	
	}
	
	
	public void setChannel(int channelId, byte[][] channel) {
		if (channelId < 0 || channelId >= channelCount)
			throw new IllegalArgumentException("channelId must be in interval [0, channelCount)");
		channels[channelId] = channel;
	}
	
	public byte[][][] getChannels() {
		return channels;
	}
	
	public Image clone() {
		Image result = new Image(type, width, height);
		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
				for (int k = 0; k < channelCount; k++)
					result.channels[k][i][j] = channels[k][i][j];
		return result;
	}
	
	public Image toGrayscale() {
		Image result = new Image(ImageType.GRAYSCALE, width, height);
		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
				result.channels[0][i][j] = (byte)(0.299f * unsignedToBytes(channels[0][i][j]) + 0.587f * unsignedToBytes(channels[1][i][j]) + 0.114f * unsignedToBytes(channels[2][i][j]));
		return result;
	}
	
	public Image shiftImage(int dx, int dy) {
		Image result = new Image(type, width, height);
		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
				for (int k = 0; k < channelCount; k++)
					result.channels[k][i][j] = channels[k][(i + dx + width) % width][(j + dy + height) % height];
		return result;
	}
}
