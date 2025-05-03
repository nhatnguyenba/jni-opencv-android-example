package com.nhatnguyenba.opencv

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageProcessorScreen() {
    var processedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var blurRadius by remember { mutableFloatStateOf(0f) }
    var currentFlipType by remember { mutableStateOf(FlipType.NONE) }
    val scope = rememberCoroutineScope()

    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showCamera by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Xử lý chọn ảnh từ gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { stream ->
                originalBitmap = BitmapFactory.decodeStream(stream)
                processedBitmap = originalBitmap
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showCamera = true
        } else {
            Toast.makeText(context, "Yêu cầu quyền truy cập camera", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Image Processor") },
                actions = {
                    IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                        Icon(Icons.Default.Photo, "Chọn ảnh")
                    }
                    IconButton(onClick = {
                        when {
                            ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED -> {
                                showCamera = true
                            }

                            else -> cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        }
                    }) {
                        Icon(Icons.Default.CameraAlt, "Chụp ảnh")
                    }
                    IconButton(onClick = {
                        originalBitmap?.let {
                            processedBitmap = it
                            blurRadius = 0f
                            currentFlipType = FlipType.NONE
                        }
                    }) {
                        Icon(Icons.Default.RestartAlt, "Reset")
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hiển thị ảnh
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                processedBitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                } ?: Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Chưa có ảnh được chọn", style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Điều khiển Flip
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Xoay ảnh", style = MaterialTheme.typography.titleSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FlipButton(
                            type = FlipType.HORIZONTAL,
                            currentType = currentFlipType,
                            onClick = { flipType ->
                                scope.launch(Dispatchers.Default) {
                                    processedBitmap = processedBitmap?.let {
                                        applyFlip(it, flipType).also {
                                            currentFlipType = flipType
                                        }
                                    }
                                }
                            }
                        )
                        FlipButton(
                            type = FlipType.VERTICAL,
                            currentType = currentFlipType,
                            onClick = { flipType ->
                                scope.launch(Dispatchers.Default) {
                                    processedBitmap = processedBitmap?.let {
                                        applyFlip(it, flipType).also {
                                            currentFlipType = flipType
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // Điều chỉnh Blur
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Độ mờ", style = MaterialTheme.typography.titleSmall)
                    Slider(
                        value = blurRadius,
                        onValueChange = { blurRadius = it },
                        valueRange = 0f..25f,
                        steps = 24,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Button(
                        onClick = {
                            scope.launch(Dispatchers.Default) {
                                processedBitmap = processedBitmap?.let { currentBmp ->
                                    applyBlur(currentBmp, blurRadius.toInt())
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Áp dụng Blur")
                    }
                }
            }
        }
    }

    // Show Camera
    if (showCamera) {
        Log.d("CAMERA_DEBUG", "Đang hiển thị camera")
        CameraCaptureView(
            onCapture = { bitmap ->
                Log.d("CAMERA_DEBUG", "Ảnh đã chụp")
                originalBitmap = bitmap
                processedBitmap = bitmap
                showCamera = false
            },
            onCancel = {
                Log.d("CAMERA_DEBUG", "Hủy chụp ảnh")
                showCamera = false
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun FlipButton(
    type: FlipType,
    currentType: FlipType,
    onClick: (FlipType) -> Unit
) {
    val (icon, label) = when (type) {
        FlipType.HORIZONTAL -> Icons.Default.Flip to "Lật ngang"
        FlipType.VERTICAL -> Icons.Default.FlipCameraAndroid to "Lật dọc"
        else -> Icons.Default.Flip to ""
    }

    FilterChip(
        selected = currentType == type,
        onClick = { onClick(type) },
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(FilterChipDefaults.IconSize)
            )
        }
    )
}

enum class FlipType { NONE, HORIZONTAL, VERTICAL }

// Hàm xử lý ảnh với Coroutine
private suspend fun applyFlip(
    bitmap: Bitmap,
    flipType: FlipType
): Bitmap? = withContext(Dispatchers.Default) {
    return@withContext when (flipType) {
        FlipType.HORIZONTAL -> {
            val w = bitmap.width
            val h = bitmap.height
            val input = ByteArray(w * h * 4)
            bitmap.copyPixelsToBuffer(java.nio.ByteBuffer.wrap(input))
            val out = NativeLib.nativeFlipHorizontal(input, w, h)
            Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
                copyPixelsFromBuffer(java.nio.ByteBuffer.wrap(out))
            }
        }

        FlipType.VERTICAL -> {
            val w = bitmap.width
            val h = bitmap.height
            val input = ByteArray(w * h * 4)
            bitmap.copyPixelsToBuffer(java.nio.ByteBuffer.wrap(input))
            val out = NativeLib.nativeFlipVertical(input, w, h)
            Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
                copyPixelsFromBuffer(java.nio.ByteBuffer.wrap(out))
            }
        }

        else -> bitmap
    }
}

private suspend fun applyBlur(bitmap: Bitmap, radius: Int): Bitmap =
    withContext(Dispatchers.Default) {
        val w = bitmap.width
        val h = bitmap.height
        val input = ByteArray(w * h * 4)
        bitmap.copyPixelsToBuffer(java.nio.ByteBuffer.wrap(input))

        val out = NativeLib.nativeBlur(input, w, h, radius)
        Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
            copyPixelsFromBuffer(java.nio.ByteBuffer.wrap(out))
        }
    }

@Composable
fun CameraCaptureView(
    onCapture: (Bitmap) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val cameraSelector = remember { CameraSelector.DEFAULT_BACK_CAMERA }
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }

                coroutineScope.launch {
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        imageCapture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .build()

                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    } catch (e: Exception) {
                        Log.e("CAMERA_ERROR", "Lỗi khởi tạo camera", e)
                        Toast.makeText(context, "Không thể khởi động camera", Toast.LENGTH_SHORT)
                            .show()
                        onCancel()
                    }
                }
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Capture controls
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Hủy")
                }

                Button(
                    onClick = {
                        val outputFile = File.createTempFile(
                            "IMG_",
                            ".jpg",
                            context.cacheDir
                        )

                        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile)
                            .build()

                        imageCapture?.takePicture(
                            outputOptions,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                    val exif = ExifInterface(outputFile.absolutePath)
                                    val orientation = exif.getAttributeInt(
                                        ExifInterface.TAG_ORIENTATION,
                                        ExifInterface.ORIENTATION_NORMAL
                                    )

                                    val bitmap = BitmapFactory.decodeFile(outputFile.absolutePath)
                                    val rotatedBitmap = when (orientation) {
                                        ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(
                                            bitmap,
                                            90f
                                        )

                                        ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(
                                            bitmap,
                                            180f
                                        )

                                        ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(
                                            bitmap,
                                            270f
                                        )

                                        else -> bitmap
                                    }
                                    onCapture(rotatedBitmap)
                                }

                                override fun onError(exc: ImageCaptureException) {
                                    Log.e("Camera", "Capture failed", exc)
                                }
                            }
                        )
                    }
                ) {
                    Text("Chụp ảnh")
                }
            }
        }
    }
}

private fun rotateBitmap(source: Bitmap, degrees: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(degrees)
    return Bitmap.createBitmap(
        source, 0, 0,
        source.width, source.height,
        matrix, true
    )
}