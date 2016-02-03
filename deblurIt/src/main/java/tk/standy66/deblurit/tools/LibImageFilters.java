package tk.standy66.deblurit.tools;

public class LibImageFilters {
    static {
        System.loadLibrary("ImageFilters");
    }

    public static native void deconvolve(byte[][] psf, byte[][][] image, int w, int h, float lambda, int kernelWidth, int kernelHeight);
    public static native void convolve(byte[][] psf, byte[][][] image, int w, int h, int kernelWidth, int kernelHeight);
    public static native void registerSubscriber(Class clazz, Object object, String methodName, String methodSignature);
    public static native void removeSubscriber();
    public static native void filterLoG(byte[][][] image, int w, int h, float radius, float alpha);
}
