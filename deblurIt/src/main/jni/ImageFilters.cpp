#include <jni.h>
#include <android/log.h>
#include <math.h>
#include <stdlib.h>
#include <time.h>
#include <pthread.h>

#define LOG_TAG "native"
#define INTERNAL
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define ERRI(...) __android_log_print(ANDROID_LOG_FATAL, LOG_TAG, __VA_ARGS__)
#define PI 3.14159265358f
#define R(x, y) x[(2 * (y))]
#define I(x, y) x[(2 * (y)) + 1]
#define pR(x, y) (x + (2 * (y)))
#define pI(x, y) (x + (2 * (y) + 1))
#define max(a, b) (((a) > (b)) ? (a) : (b))

typedef void (*GeneralizedConvolutionFunction)(float*, float*, int, int, float);

extern "C" {

    jmethodID gMethod = 0;
    jobject gSubscriber = 0;
    JNIEnv *lastEnv = 0;

    INTERNAL inline int publish_progress(int progress) {
        // we assume that if we have no subsciber, then calling process is killed, so we just return 1
        if (gMethod == 0 || lastEnv == 0 || gSubscriber == 0) {
            LOGI("Ret is one");
            return 1;
        }
        jint ret = lastEnv->CallIntMethod(gSubscriber, gMethod, progress);
        LOGI("Ret is %d", ret);
        return ret;
    }

    INTERNAL void swap(float *a, float *b) {
        float tmp = *a;
        *a = *b;
        *b = tmp;
    }

    INTERNAL inline int bitreverse(int x, int lg_n) {
        int res = 0;
        for (int i = 0; i < lg_n; i++)
            if (x & (1 << i))
                res |= (1 << (lg_n - i - 1));
        return res;
    }

    int *brTable = 0;
    INTERNAL inline void precalc_bitreverse(int maxn) {
        if (brTable != 0)
            free(brTable);
        brTable = (int*)malloc(maxn * sizeof(int));
        int lg_n = 0;
        while ((1 << lg_n) < maxn) ++lg_n;
        for (int i = 0; i < maxn; i++) {
            brTable[i] = bitreverse(i, lg_n);
        }
    }

    float *tx = 0;
    float *ty = 0;
    INTERNAL inline void precal_twiddels_forward(int n) {
        if (tx != 0)
            free(tx);
        if (ty != 0)
            free(ty);
        tx = (float*)malloc(n * n * sizeof(float));
        ty = (float*)malloc(n * n * sizeof(float));
        int k = 0;
        for (int len = 2; len <= n; len <<= 1, k++) {
            float angle = -2 * PI / len;
            float cosa = cosf(angle);
            float sina = sinf(angle);
            float x = 1;
            float y = 0;
            for (int j = 0; j < len / 2; j++) {
                tx[k * n + j] = x;
                ty[k * n + j] = y;
                float x0 = x * cosa - y * sina;
                y = x * sina + y * cosa;
                x = x0;
            }
        }
    }


    float *itx = 0;
    float *ity = 0;
    INTERNAL inline void precal_twiddels_inverse(int n) {
        if (itx != 0)
            free(itx);
        if (ity != 0)
            free(ity);
        itx = (float*)malloc(n * n * sizeof(float));
        ity = (float*)malloc(n * n * sizeof(float));
        int k = 0;
        for (int len = 2; len <= n; len <<= 1, k++) {
            float angle = 2 * PI / len;
            float cosa = cosf(angle);
            float sina = sinf(angle);
            float x = 1;
            float y = 0;
            for (int j = 0; j < len / 2; j++) {
                itx[k * n + j] = x;
                ity[k * n + j] = y;
                float x0 = x * cosa - y * sina;
                y = x * sina + y * cosa;
                x = x0;
            }
        }
    }

    INTERNAL float* fft_1d(float* data, int n, bool bitReverse = false, bool inverse = false) {
        n /= 2;
        if (!bitReverse)
            for (int i = 0; i < n; i++) {
                int x = brTable[i];
                if (i < x) {
                    swap(pR(data, i), pR(data, x));
                    swap(pI(data, i), pI(data, x));
                }
            }
        int k = 0;
        for (int len = 2; len <= n; len <<= 1, k++)
            for (int i = 0; i < n; i += len)
                for (int j = 0; j < len / 2; j++) {
                    float x = inverse ? itx[k * n + j] : tx[k * n + j];
                    float y = inverse ? ity[k * n + j] : ty[k * n + j];
                    int c = (i + j) * 2;
                    float *ux = data + c;
                    float *uy = data + c + 1;
                    float *a = data + c + len;
                    float *b = data + c + len + 1;
                    float vx = *a * x - *b * y;
                    float vy = *b * x + *a * y;
                    *a = *ux - vx;
                    *b = *uy - vy;
                    *ux += vx;
                    *uy += vy;
                }
        return 0;
    }

    INTERNAL float* fft2d(float* data, int w, int h, int &progress) {
        long l = clock();
        int n = w * h;
        float* aligned = (float*)calloc(2 * n, sizeof(float));
        for (int i = 0; i < n; i++)
            aligned[2 * i] = data[i];
        for (int i = 0; i < w; i++) {
            fft_1d(aligned + 2 * i * h, 2 * h, false, false);
            progress--;
            if (!(i % 500))
                if (publish_progress(progress) != 0) {
                    free(aligned);
                    return NULL;
                }
        }
        LOGI("Here");
        float* transposed = (float*)malloc(sizeof(float) * 2 * n);
        for (int i = 0; i < w; i++)
            for (int j = 0; j < h; j++) {
                R(transposed, i * h + j) = R(aligned, j * w + i);
                I(transposed, i * h + j) = I(aligned, j * w + i);
            }
        for (int i = 0; i < h; i++) {
            fft_1d(transposed + 2 * i * w, 2 * w, false, false);
            progress--;
            if (!(i % 500))
                if (publish_progress(progress) != 0) {
                    free(aligned);
                    free(transposed);
                    return NULL;
                }

        }
        free(aligned);
        LOGI("FFT2D time: %f ms", (float)(clock() - l) / 1000);
        return transposed;
    }

    INTERNAL float* ifft2d(float* data, int w, int h, int &progress) {
        long l = clock();
        int n = 2 * w * h;
        for (int i = 0; i < w; i++) {
            fft_1d(data + 2 * i * h, 2 * h, false, true);
            progress--;
            if (!(i % 500))
                if (publish_progress(progress) != 0) return NULL;
        }
        float* transposed = (float*)malloc(sizeof(float) * n);
        for (int i = 0; i < w; i++)
            for (int j = 0; j < h; j++) {
                R(transposed, i * h + j) = R(data, j * w + i);
                I(transposed, i * h + j) = I(data, j * w + i);
            }
        for (int i = 0; i < h; i++) {
            fft_1d(transposed + 2 * i * w, 2 * w, false, true);
            progress--;
            if (!(i % 500)) {
                if (publish_progress(progress) != 0) {
                    free(transposed);
                    return NULL;
                }
            }
        }
        float* ans = (float*)malloc(sizeof(float) * n / 2);
        for (int i = 0; i < n; i+=2)
            ans[i / 2] = transposed[i];
        free(transposed);
        LOGI("IFFT2D time: %f ms", (float)(clock() - l) / 1000);
        return ans;
    }

    INTERNAL void deconvolve_ffts(float* kernelFFT, float* channelFFT, int w, int h, float lambda) {
        for (int i = 0; i < w * h; i++) {
            float a = channelFFT[2 * i];
            float b = channelFFT[2 * i + 1];
            float c = kernelFFT[2 * i];
            float d = -kernelFFT[2 * i + 1];
            float magnitude2 = c * c + d * d + lambda;
            c /= magnitude2;
            d /= magnitude2;
            channelFFT[2 * i] = a * c - b * d;
            channelFFT[2 * i + 1] = b * c + a * d;
        }
    }

    INTERNAL void convolve_ffts(float* kernelFFT, float* channelFFT, int w, int h, float lambda) {
        for (int i = 0; i < w * h; i++) {
            float a = channelFFT[2 * i];
            float b = channelFFT[2 * i + 1];
            float c = kernelFFT[2 * i];
            float d = kernelFFT[2 * i + 1];
            channelFFT[2 * i] = a * c - b * d;
            channelFFT[2 * i + 1] = b * c + a * d;
        }
    }



    INTERNAL void init(int maxn) {
        long time = clock();
        precal_twiddels_forward(maxn);
        precal_twiddels_inverse(maxn);
        LOGI("Twiddels precalc time: %f ms", (float)(clock() - time) / 1000);
        time = clock();
        precalc_bitreverse(maxn);
        LOGI("Bitreverse precalc time: %f ms", (float)(clock() - time) / 1000);
    }

    float eps = 1e-3;

    bool equals(float a, float b) {
        return abs(a - b) < eps;
    }

    INTERNAL float* laplacianOfGaussian(float radius, int wceil, int hceil) {
        LOGI("Radius: %f", radius);
        float* pimage = (float*)calloc(wceil * hceil, sizeof(float));
        float center = 4 * radius;
        float a = 1 / (PI * radius * radius * radius * radius);
        for (int i = 0; i <= 2 * center; i++)
            for (int j = 0; j <= 2 * center; j++) {
                float dx = i - center;
                float dy = j - center;
                float exponent = expf((-dx * dx - dy * dy) / (2 * radius * radius));
                float p = 1 - (dx*dx + dy*dy)/ (2 * radius * radius);
                float result = a * p * exponent;
                pimage[i * hceil + j] = a * p * exponent;
            }
        return pimage;
    }

    INTERNAL float* fetchImage(JNIEnv *env, jobjectArray image, int w, int h, int wceil, int hceil) {
        long time = clock();
        float* pimage = (float*)calloc(wceil * hceil, sizeof(float));
        for (int i = 0; i < w; i++) {
            jbyteArray row = (jbyteArray)env->GetObjectArrayElement(image, i);
            jbyte* prow = (jbyte*)env->GetByteArrayElements(row, 0);
            for (int j = 0; j < h; j++)
                pimage[i * hceil + j] = (unsigned char)prow[j];
            env->ReleaseByteArrayElements(row, prow, JNI_OK);
            env->DeleteLocalRef(row);
        }
        LOGI("Image fetching time: %f ms", (float)(clock() - time) / 1000);
        return pimage;
    }

    INTERNAL float* shiftImage(float* image, int w, int h, int shiftx, int shifty) {
        float* newimage = (float*)malloc(w * h * sizeof(float));
        for (int i = 0; i < w; i++)
            for (int j = 0; j < h; j++) {
                newimage[i * h + j] = image[((i - shiftx + w) % w) * h + (j - shifty + h) % h];
            }
        free(image);
        return newimage;
    }

    INTERNAL void normalize(float* image, int w, int h) {
        float normalizationCoefficient = 0;
        for (int i = 0; i < w * h; i++)
            normalizationCoefficient += image[i];
        for (int i = 0; i < w * h; i++)
            image[i] /= normalizationCoefficient;

        /*float min = 1000000;
        float max = -1000000;
        for (int i = 0; i < w * h; i++) {
            if (image[i] < min)
                min = image[i];
            if (image[i] > max)
                max = image[i];
        }
        for (int i = 0; i < w * h; i++)
            image[i] -= min;
        for (int i = 0; i < w * h; i++)
            image[i] *= (255) / (max - min) * w * h;*/

    }

    INTERNAL void scale(float* image, int w, int h, float alpha) {
        for (int i = 0; i < w * h; i++)
            image[i] *= alpha;
    }

    INTERNAL void writeImage(JNIEnv *env, float* pimage, jobjectArray image, int w, int h, int wceil, int hceil) {
        for (int i = 0; i < w; i++) {
            jbyteArray row = (jbyteArray)env->GetObjectArrayElement(image, i);
            jboolean copyMade = 0;
            jbyte* prow = (jbyte*)env->GetByteArrayElements(row, &copyMade);
            for (int j = 0; j < h; j++) {
                int val = (int)(pimage[i * hceil + j] / wceil / hceil);
                if (val < 0) val = 0;
                if (val > 255) val = 255;
                prow[j] = (signed char)val;
            }
            env->ReleaseByteArrayElements(row, prow, JNI_OK);
            env->DeleteLocalRef(row);
        }
    }

    JNIEXPORT void JNICALL Java_tk_standy66_deblurit_tools_LibImageFilters_registerSubscriber(JNIEnv *env, jobject *obj, jclass clazz, jobject object, jstring methodName, jstring methodSignature) {
        const char* pmethodName = env->GetStringUTFChars(methodName, 0);
        const char* pmethodSignature = env->GetStringUTFChars(methodSignature, 0);
        gMethod = env->GetMethodID(clazz, pmethodName, pmethodSignature);
        gSubscriber = env->NewGlobalRef(object);
        LOGI("Subscriber registered");
    }

    JNIEXPORT void JNICALL Java_tk_standy66_deblurit_tools_LibImageFilters_removeSubscriber(JNIEnv *env, jobject *obj) {
        gMethod = 0;
        env->DeleteGlobalRef(gSubscriber);
        gSubscriber = 0;
    }

    INTERNAL void laplacianOfGaussianFiltering(JNIEnv *env, jobject *obj, jobjectArray image, int w, int h, float radius, float alpha) {
        long wholeRunTime = clock();
        int k = env->GetArrayLength(image);
        lastEnv = env;
        int kernelWidth = 8 * radius;
        int kernelHeight = 8 * radius;
        int wceil, hceil, log_w = 0, log_h = 0;
        while ((1 << log_w) < max(w, kernelWidth)) ++log_w;
        while ((1 << log_h) < max(h, kernelHeight)) ++log_h;
        wceil = 1 << log_w;
        hceil = 1 << log_h;
        int maxceil = max(wceil, hceil);
        int progress = 2 * maxceil * (2 * k + 1);
        LOGI("Got image: %d %d, resized to: %d %d", w, h, wceil, hceil);
        LOGI("Kernel dimens are: %d %d", kernelWidth, kernelHeight);
        init(maxceil);
        float* kernel = laplacianOfGaussian(radius, maxceil, maxceil);
        normalize(kernel, maxceil, maxceil);
        kernel = shiftImage(kernel, maxceil, maxceil, -kernelWidth / 2, -kernelHeight / 2);
        //scale and add diracDelta
        scale(kernel, maxceil, maxceil, alpha);
        kernel[0] += 1;
        float* kernelFFT = fft2d(kernel, maxceil, maxceil, progress);
        if (kernelFFT == NULL) {
            free(kernel);
            return;
        }
        free(kernel);
        for (int c = 0; c < k; c++) {
            jobjectArray g = (jobjectArray)env->GetObjectArrayElement(image, c);
            float* channel = fetchImage(env, g, w, h, maxceil, maxceil);
            float* channelFFT = fft2d(channel, maxceil, maxceil, progress);
            if (channelFFT == NULL) {
                free(kernelFFT);
                free(channel);
                return;
            }
            free(channel);
            convolve_ffts(kernelFFT, channelFFT, maxceil, maxceil, 0);
            channel = ifft2d(channelFFT, maxceil, maxceil, progress);
            if (channel == NULL) {
                free(kernelFFT);
                free(channelFFT);
                return;
            }
            free(channelFFT);
            writeImage(env, channel, g, w, h, maxceil, maxceil);
            free(channel);
            env->DeleteLocalRef(g);
        }
        free(kernelFFT);
        LOGI("Native time: %fms", (clock() - wholeRunTime) / 1000.0f);
    }

    INTERNAL void generalizedConvolution(JNIEnv *env, jobject *obj, jobjectArray psf, jobjectArray image, int w, int h, float lambda, int kernelWidth, int kernelHeight, GeneralizedConvolutionFunction func) {
        long wholeRunTime = clock();
        int k = env->GetArrayLength(image);
        lastEnv = env;
        int wceil, hceil, log_w = 0, log_h = 0;
        while ((1 << log_w) < max(w, kernelWidth)) ++log_w;
        while ((1 << log_h) < max(h, kernelHeight)) ++log_h;
        wceil = 1 << log_w;
        hceil = 1 << log_h;
        int maxceil = max(wceil, hceil);
        int progress = 2 * maxceil * (2 * k + 1);
        LOGI("Got image: %d %d, resized to: %d %d", w, h, wceil, hceil);
        LOGI("Kernel dimens are: %d %d", kernelWidth, kernelHeight);
        init(maxceil);
        float* kernel = fetchImage(env, psf, kernelWidth, kernelHeight, maxceil, maxceil);
        normalize(kernel, maxceil, maxceil);
        kernel = shiftImage(kernel, maxceil, maxceil, -kernelWidth / 2, -kernelHeight / 2);
        float* kernelFFT = fft2d(kernel, maxceil, maxceil, progress);
        if (kernelFFT == NULL) {
            free(kernel);
            return;
        }
        free(kernel);
        for (int c = 0; c < k; c++) {
            jobjectArray g = (jobjectArray)env->GetObjectArrayElement(image, c);
            float* channel = fetchImage(env, g, w, h, maxceil, maxceil);
            float* channelFFT = fft2d(channel, maxceil, maxceil, progress);
            if (channelFFT == NULL) {
                free(kernelFFT);
                free(channel);
                return;
            }
            free(channel);
            (*func)(kernelFFT, channelFFT, maxceil, maxceil, lambda);
            channel = ifft2d(channelFFT, maxceil, maxceil, progress);
            if (channel == NULL) {
                free(kernelFFT);
                free(channelFFT);
                return;
            }
            free(channelFFT);
            writeImage(env, channel, g, w, h, maxceil, maxceil);
            free(channel);
            env->DeleteLocalRef(g);
        }
        free(kernelFFT);
        LOGI("Native time: %fms", (clock() - wholeRunTime) / 1000.0f);
    }

    JNIEXPORT void JNICALL Java_tk_standy66_deblurit_tools_LibImageFilters_filterLoG(JNIEnv *env, jobject *obj, jobjectArray image, int w, int h, float radius, float alpha) {
        laplacianOfGaussianFiltering(env, obj, image, w, h, radius, alpha);
        LOGI("LoG sharpening success");
    }


    JNIEXPORT void JNICALL Java_tk_standy66_deblurit_tools_LibImageFilters_deconvolve(JNIEnv *env, jobject *obj, jobjectArray psf, jobjectArray image, int w, int h, float lambda, int kernelWidth, int kernelHeight) {
        generalizedConvolution(env, obj, psf, image, w, h, lambda, kernelWidth, kernelHeight, deconvolve_ffts);
        LOGI("Deconvolution success");
    }

    JNIEXPORT void JNICALL Java_tk_standy66_deblurit_tools_LibImageFilters_convolve(JNIEnv *env, jobject *obj, jobjectArray psf, jobjectArray image, int w, int h, int kernelWidth, int kernelHeight) {
        generalizedConvolution(env, obj, psf, image, w, h, 0, kernelWidth, kernelHeight, convolve_ffts);
        LOGI("Convolution success");
    }

    static double now_ms() {
        struct timespec res;
        clock_gettime(CLOCK_REALTIME, &res);
        return 1000.0 * res.tv_sec + (double) res.tv_nsec / 1e6;
    }

    int size = 100000000;
    int num_threads = 4;
    long long* thread_res;
    char* ar;

    void* worker(void* arg) {
        int thread_id = *((int*)arg);
        int block_size = size / num_threads;
        int start = thread_id * block_size;
        int end = start + block_size;
        long long sum = 0;
        for (int i = start; i < end; ++i) {
            sum += ar[i];
        }
        thread_res[thread_id] = sum;
        return NULL;
    }

    JNIEXPORT void JNICALL Java_tk_standy66_deblurit_tools_LibImageFilters_test(JNIEnv *env, jobject *obj) {
        LOGI("In test");

        thread_res = (long long*) malloc(sizeof(long long) * num_threads);
        ar = (char*) malloc(sizeof(char) * size);

        for (int i = 0; i < size; ++i) {
            ar[i] = (char) i;
        }

        double start_time = now_ms();
        int* thread_ids;
        pthread_t* threads;

        threads = (pthread_t*) malloc(sizeof(pthread_t) * num_threads);
        thread_ids = (int*) malloc(sizeof(int) * num_threads);
        for (int i = 0; i < num_threads; ++i) {
            thread_ids[i] = i;
        }

        for (int i = 0; i < num_threads; ++i) {
            pthread_create(&threads[i], NULL, worker, &thread_ids[i]);
        }
        for (int i = 0; i < num_threads; ++i) {
            void* val;
            pthread_join(threads[i], &val);
        }
        //worker(thread_ids);

        long long final_sum = 0;
        for (int i = 0; i < num_threads; ++i) {
            final_sum += thread_res[i];
        }

        free(thread_ids);
        free(threads);
        double delta = now_ms() - start_time;
        LOGI("Delta time: %f", delta);
        free(ar);
        free(thread_res);
    }
}
