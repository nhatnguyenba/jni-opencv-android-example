package com.nhatnguyenba.opencv

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ImageProcessorScreen()
            }
        }
    }
}

@Composable
fun ImageProcessorScreen() {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var processed by remember { mutableStateOf<Bitmap?>(null) }
    var blurSize by remember { mutableStateOf(15) }
    val context = LocalContext.current

    // Launcher để chọn ảnh
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val stream = context.contentResolver.openInputStream(it)
            bitmap = BitmapFactory.decodeStream(stream)
            processed = bitmap
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(onClick = { launcher.launch("image/*") }) {
            Text("Chọn ảnh")
        }
        bitmap?.let { img ->
            Image(
                bitmap = img.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
        }
        processed?.let { img ->
            Image(
                bitmap = img.asImageBitmap(),
                contentDescription = "Ảnh đã xử lý",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                bitmap?.let {
                    val w = it.width
                    val h = it.height
                    val input = ByteArray(w * h * 4)
                    it.copyPixelsToBuffer(java.nio.ByteBuffer.wrap(input))
                    val out = NativeLib.nativeFlip(input, w, h)
                    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                    bmp.copyPixelsFromBuffer(java.nio.ByteBuffer.wrap(out))
                    processed = bmp
                }
            }) {
                Text("Flip Ảnh")
            }
            Button(onClick = {
                bitmap?.let {
                    val w = it.width
                    val h = it.height
                    val input = ByteArray(w * h * 4)
                    it.copyPixelsToBuffer(java.nio.ByteBuffer.wrap(input))
                    val out = NativeLib.nativeBlur(input, w, h, blurSize)
                    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                    bmp.copyPixelsFromBuffer(java.nio.ByteBuffer.wrap(out))
                    processed = bmp
                }
            }) {
                Text("Blur Ảnh")
            }
        }
    }
}