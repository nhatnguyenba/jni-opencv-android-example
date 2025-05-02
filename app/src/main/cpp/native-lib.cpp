#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>
#include <vector>

extern "C" JNIEXPORT jstring JNICALL
Java_com_nhatnguyenba_opencv_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

// Chuyển mảng byte Android sang cv::Mat
static cv::Mat byteArrayToMat(JNIEnv* env, jbyteArray arr, int width, int height) {
    jbyte* data = env->GetByteArrayElements(arr, nullptr);
    cv::Mat mat(height, width, CV_8UC4, reinterpret_cast<unsigned char*>(data));
    cv::Mat matCopy = mat.clone();
    env->ReleaseByteArrayElements(arr, data, JNI_ABORT);
    return matCopy;
}

// Chuyển cv::Mat sang mảng byte để trả về Android
static jbyteArray matToByteArray(JNIEnv* env, const cv::Mat& mat) {
    int size = mat.total() * mat.elemSize();
    jbyteArray arr = env->NewByteArray(size);
    env->SetByteArrayRegion(arr, 0, size, reinterpret_cast<const jbyte*>(mat.data));
    return arr;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_nhatnguyenba_opencv_NativeLib_nativeFlip(
        JNIEnv* env, jobject , jbyteArray inputImage,
        jint width, jint height) {
    cv::Mat mat = byteArrayToMat(env, inputImage, width, height);
    cv::Mat flipped;
    cv::flip(mat, flipped, 1); // ngang
    return matToByteArray(env, flipped);
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_nhatnguyenba_opencv_NativeLib_nativeBlur(
        JNIEnv* env, jobject , jbyteArray inputImage,
        jint width, jint height, jint blurSize) {
    cv::Mat mat = byteArrayToMat(env, inputImage, width, height);
    cv::Mat blurred;
    cv::blur(mat, blurred, cv::Size(blurSize, blurSize));
    return matToByteArray(env, blurred);
}