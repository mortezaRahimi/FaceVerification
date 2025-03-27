# Gender & Reality Photo Verifier (Android + Compose)


This Android library provides a simple and efficient way to verify if a photo contains a man, woman, or is a real person. It leverages on-device machine learning to provide fast and accurate results.

**Key Features:**

* **Gender Detection:** Accurately identifies if a photo contains a man or a woman.
* **Reality Check:** Determines if the photo depicts a real person or not.
* **Easy Integration:** Simple API for quick implementation.
* **On-Device Processing:** No need for network requests, ensuring privacy and speed.
* **Kotlin-First:** Built with Kotlin, offering a modern and concise API.


**Usage:**

1.  **Request Storage Permissions:**

    Before using the library, ensure your application has the `READ_EXTERNAL_STORAGE` permission.

    ```kotlin
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
    } else {
        // Permission already granted, proceed
        processImage()
    }
    ```


2.  **Load the Image:**

    Load the image from the user's storage.

   
    val imageUri: Uri = // Get the image URI from the user's selection
    val imageBitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
    ```

3.  **Initialize the Verifier and Process:**

    Create an instance of the `PhotoVerifier` and process the image.

    ```kotlin

     val verificationResult =
        remember { mutableStateOf<PhotoVerification.VerificationResult?>(null) }

        verificationResult.value?.let { result ->
                Text("Real Person: ${result.isRealPerson}")
                Text("Gender: ${result.gender}")
                if (result.errorMessage != null) {
                    Text("Error: ${result.errorMessage}")
                }
            }

    //Check sample app
  
    ```

**PhotoVerificationResult:**

The `verify` function's callback returns a `PhotoVerificationResult` which can be:

* **`PhotoVerificationResult.Success(gender: Gender, isRealPerson: Boolean)`:**
    * `gender`: An enum `Gender.Male` or `Gender.Female` or `Gender.Unknown`
    * `isRealPerson`: A boolean indicating whether the photo depicts a real person.
* **`PhotoVerificationResult.Error(exception: Exception)`:** An error occurred during verification.

**Important Notes:**

* **Storage Permission:** Your application *must* request and be granted `READ_EXTERNAL_STORAGE` permission before using this library.
* **Performance:** On-device processing provides good performance, but processing time may vary depending on the device and image size.
* **Accuracy:** While the library strives for high accuracy, machine learning models are not perfect. Consider the results as estimations.
* **Privacy:** Because everything is processed on device, the image data never leaves the user's device.
* **Dependencies:** Please check the project's build.gradle in your repo, for required dependencies for minimum android api levels and machine learning support.

**Contributing:**

Contributions are welcome! Please feel free to submit pull requests or open issues.

**License:**

This library is released under the MIT License. See `LICENSE` for details.
