package com.mortex.faceverification

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mortex.photoverification.PhotoVerification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@Composable
fun PhotoVerificationScreen(paddingValues: PaddingValues) {
    val context = LocalContext.current
    val bitmap = remember { mutableStateOf<Bitmap?>(null) }
    val verificationResult =
        remember { mutableStateOf<PhotoVerification.VerificationResult?>(null) }
    val permissionGranted = remember { mutableStateOf(false) }
    var selectedMediaUri by remember { mutableStateOf<Uri?>(null) }

    val mediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            selectedMediaUri = uri

        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            permissionGranted.value = isGranted
            if (isGranted) {
                mediaLauncher.launch(PickVisualMediaRequest()) // or ImageOnly or VideoOnly
            }
        }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            bitmap.value = uriToBitmap(context, it)
        }
    }

    Column(Modifier.padding(paddingValues)) {

        if (!permissionGranted.value) {
            Text("Please grant storage permission.")
            Button(onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }

            }) {
                Text("Request Permission")
            }
        } else {
            Button(onClick = {
                launcher.launch("image/*")
            }) {
                Text("Select Photo")
            }

            bitmap.value?.let {
                Image(bitmap = it.asImageBitmap(), contentDescription = "Selected Photo")
                PhotoVerificationCompose(bitmap = it) { result ->
                    verificationResult.value = result
                }
            }

            verificationResult.value?.let { result ->
                Text("Real Person: ${result.isRealPerson}")
                Text("Gender: ${result.gender}")
                if (result.errorMessage != null) {
                    Text("Error: ${result.errorMessage}")
                }
            }
        }
    }
}

@Composable
fun PhotoVerificationCompose(
    bitmap: Bitmap?,
    onResult: (PhotoVerification.VerificationResult) -> Unit
) {
    val context = LocalContext.current
    val photoVerification = remember { PhotoVerification(context) }

    LaunchedEffect(bitmap) {
        if (bitmap != null) {
            withContext(Dispatchers.IO) {
                val result = photoVerification.verifyPhoto(bitmap)
                withContext(Dispatchers.Main) {
                    onResult(result)
                }
            }
        }
    }

}

private fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        } else {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}