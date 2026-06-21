package com.baer.memolio.core.media

import android.graphics.BitmapFactory
import com.baer.memolio.core.di.IoDispatcher
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.Face
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MlKitFaceDetector @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : FaceDetector {

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .build()

    private val client by lazy { FaceDetection.getClient(options) }

    override suspend fun detectFocalPoint(jpeg: File): FocalPoint? = withContext(ioDispatcher) {
        val bitmap = BitmapFactory.decodeFile(jpeg.absolutePath) ?: return@withContext null
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val detectedFaces: List<Face>? = suspendCoroutine { cont ->
                client.process(image)
                    .addOnSuccessListener { result: List<Face> -> cont.resume(result) }
                    .addOnFailureListener { cont.resume(null) }
            }
            if (detectedFaces == null) return@withContext null

            val boxes: List<FaceBox> = detectedFaces.map { face ->
                val r = face.boundingBox
                FaceBox(r.left, r.top, r.right, r.bottom)
            }
            computeFocalPoint(boxes, bitmap.width, bitmap.height)
        } finally {
            bitmap.recycle()
        }
    }
}
