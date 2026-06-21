package com.baer.memolio.core.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FocalMathTest {

    private val delta = 0.001f

    @Test
    fun `empty faces list returns null`() {
        assertNull(computeFocalPoint(emptyList(), 100, 100))
    }

    @Test
    fun `zero imageWidth returns null`() {
        assertNull(computeFocalPoint(listOf(FaceBox(10, 10, 20, 20)), 0, 100))
    }

    @Test
    fun `zero imageHeight returns null`() {
        assertNull(computeFocalPoint(listOf(FaceBox(10, 10, 20, 20)), 100, 0))
    }

    @Test
    fun `single centered face returns 0_5 x 0_5`() {
        // box 40..60 in each axis in a 100x100 image → center = 50,50 → 0.5, 0.5
        val result = computeFocalPoint(listOf(FaceBox(40, 40, 60, 60)), 100, 100)
        checkNotNull(result)
        assertEquals(0.5f, result.x, delta)
        assertEquals(0.5f, result.y, delta)
    }

    @Test
    fun `single face in top-left returns approximately 0_1`() {
        // box left=0, top=0, right=20, bottom=20 in 100x100 → center = 10,10 → 0.1, 0.1
        val result = computeFocalPoint(listOf(FaceBox(0, 0, 20, 20)), 100, 100)
        checkNotNull(result)
        assertEquals(0.1f, result.x, delta)
        assertEquals(0.1f, result.y, delta)
    }

    @Test
    fun `two faces far apart focal point is center of union`() {
        // Face 1: left=0, top=0, right=10, bottom=10
        // Face 2: left=90, top=90, right=100, bottom=100
        // Union: left=0, top=0, right=100, bottom=100 → center = 50,50 → 0.5, 0.5
        val faces = listOf(
            FaceBox(0, 0, 10, 10),
            FaceBox(90, 90, 100, 100),
        )
        val result = computeFocalPoint(faces, 100, 100)
        checkNotNull(result)
        assertEquals(0.5f, result.x, delta)
        assertEquals(0.5f, result.y, delta)
    }

    @Test
    fun `result is clamped to 0f 1f when face box exceeds image bounds`() {
        // Box extends beyond image width: right=150, bottom=150 on a 100x100 image
        // center = (0+150)/2=75, normalized = 75/100=0.75, clamped stays 0.75
        // But if box is entirely out: left=110, top=110, right=200, bottom=200
        // center = 155, normalized = 1.55 → clamped to 1.0
        val result = computeFocalPoint(listOf(FaceBox(110, 110, 200, 200)), 100, 100)
        checkNotNull(result)
        assertEquals(1.0f, result.x, delta)
        assertEquals(1.0f, result.y, delta)
    }
}
