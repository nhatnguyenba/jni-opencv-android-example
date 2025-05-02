package com.nhatnguyenba.opencv

object NativeLib {
    init {
        System.loadLibrary("native-lib")
    }

    external fun nativeFlip(inputImage: ByteArray, width: Int, height: Int): ByteArray
    external fun nativeBlur(inputImage: ByteArray, width: Int, height: Int, blurSize: Int): ByteArray
}
