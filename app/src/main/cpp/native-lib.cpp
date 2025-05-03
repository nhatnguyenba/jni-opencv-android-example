#include <jni.h>
#include <opencv2/opencv.hpp>
#include <android/bitmap.h>

using namespace cv;

extern "C" {

JNIEXPORT jbyteArray JNICALL
Java_com_nhatnguyenba_opencv_NativeLib_nativeFlipHorizontal(
        JNIEnv *env,
        jobject thiz,
        jbyteArray input,
        jint w,
        jint h) {
    jbyte *inputBytes = env->GetByteArrayElements(input, nullptr);
    Mat src(h, w, CV_8UC4, (uchar *) inputBytes);
    Mat dst;

    // Flip theo trục ngang (1: horizontal, 0: vertical)
    flip(src, dst, 1);

    jbyteArray result = env->NewByteArray(dst.total() * dst.elemSize());
    env->SetByteArrayRegion(result, 0, dst.total() * dst.elemSize(), (jbyte *) dst.data);
    env->ReleaseByteArrayElements(input, inputBytes, 0);
    return result;
}

JNIEXPORT jbyteArray JNICALL
Java_com_nhatnguyenba_opencv_NativeLib_nativeFlipVertical(
        JNIEnv *env,
        jobject thiz,
        jbyteArray input,
        jint w,
        jint h) {
    jbyte *inputBytes = env->GetByteArrayElements(input, nullptr);
    Mat src(h, w, CV_8UC4, (uchar *) inputBytes);
    Mat dst;

    // Flip theo trục dọc
    flip(src, dst, 0);

    jbyteArray result = env->NewByteArray(dst.total() * dst.elemSize());
    env->SetByteArrayRegion(result, 0, dst.total() * dst.elemSize(), (jbyte *) dst.data);
    env->ReleaseByteArrayElements(input, inputBytes, 0);
    return result;
}

JNIEXPORT jbyteArray JNICALL
Java_com_nhatnguyenba_opencv_NativeLib_nativeBlur(
        JNIEnv *env,
        jobject thiz,
        jbyteArray input,
        jint w,
        jint h,
        jint radius) {
    jbyte *inputBytes = env->GetByteArrayElements(input, nullptr);
    Mat src(h, w, CV_8UC4, (uchar *) inputBytes);
    Mat dst;

    // Làm mờ ảnh với Gaussian Blur
    GaussianBlur(src, dst, Size(2 * radius + 1, 2 * radius + 1), 0);

    jbyteArray result = env->NewByteArray(dst.total() * dst.elemSize());
    env->SetByteArrayRegion(result, 0, dst.total() * dst.elemSize(), (jbyte *) dst.data);
    env->ReleaseByteArrayElements(input, inputBytes, 0);
    return result;
}

}