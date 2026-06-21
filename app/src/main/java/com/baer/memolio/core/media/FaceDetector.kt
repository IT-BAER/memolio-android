package com.baer.memolio.core.media

import java.io.File

interface FaceDetector {
    /** Detect faces in the given JPEG and return a normalized focal point, or null if none / undetectable. */
    suspend fun detectFocalPoint(jpeg: File): FocalPoint?
}
