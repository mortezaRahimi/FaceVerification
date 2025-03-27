package com.mortex.photoverification

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.tasks.await

class PhotoVerification(private val context: Context) {

    private val realPersonThreshold = 0.8f // Adjust as needed
    private val faceDetectorOptions = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .enableTracking()
        .build()

    private val faceDetector = FaceDetection.getClient(faceDetectorOptions)

    suspend fun verifyPhoto(bitmap: Bitmap): VerificationResult {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val faces = faceDetector.process(image).await()

            if (faces.isEmpty()) {
                VerificationResult(false, null, "No face detected.")
            } else {
                val face = faces.first() // Assuming only one face for simplicity.

                val isRealPerson = isRealPerson(face)
                val gender = determineGender(face)

                VerificationResult(isRealPerson, gender, null)
            }
        } catch (e: Exception) {
            Log.e("PhotoVerification", "Error during verification: ${e.message}", e)
            VerificationResult(false, null, "Verification failed: ${e.message}")
        }
    }

    private fun isRealPerson(face: Face): Boolean {
        val leftEyeOpenProbability = face.leftEyeOpenProbability ?: 0.0f
        val rightEyeOpenProbability = face.rightEyeOpenProbability ?: 0.0f

        val eyesOpen =
            leftEyeOpenProbability > 0.3f && rightEyeOpenProbability > 0.3f; //adjust threshold

        return eyesOpen
    }

    private fun determineGender(face: Face): Gender? {

        val smilingProbability = face.smilingProbability ?: -1f


        if (smilingProbability == -1f) return null

        if (smilingProbability > 0.5f) {
            return Gender.FEMALE
        } else {
            return Gender.MALE
        }
    }

    data class VerificationResult(
        val isRealPerson: Boolean,
        val gender: Gender?,
        val errorMessage: String?
    )

    enum class Gender {
        MALE,
        FEMALE,
        OTHER,
        UNKNOWN
    }
}