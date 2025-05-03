package com.nhatnguyenba.opencv

object NativeLib {
    init {
        System.loadLibrary("native-lib")
    }

    // Blur
    external fun nativeBlur(input: ByteArray, w: Int, h: Int, radius: Int): ByteArray

    // Flip
    external fun nativeFlipHorizontal(input: ByteArray, w: Int, h: Int): ByteArray
    external fun nativeFlipVertical(input: ByteArray, w: Int, h: Int): ByteArray
}