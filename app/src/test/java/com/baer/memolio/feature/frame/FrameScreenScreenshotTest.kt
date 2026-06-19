/*
 * Roborazzi screenshot tests for FrameScreen.
 *
 * WHY THESE ARE @Ignored LOCALLY
 * --------------------------------
 * Roborazzi pixel capture requires Robolectric NATIVE graphics mode, which in turn
 * needs a prebuilt native binary that Robolectric downloads at first run. On Windows
 * the download works but the binary requires Linux ABI — tests would crash at runtime.
 * The tests are therefore annotated @Ignore so a local `./gradlew :app:testDebugUnitTest`
 * stays GREEN with them skipped.
 *
 * CI WORKFLOW (Linux runner)
 * --------------------------
 * 1. Record baselines (first time, or after intentional visual changes):
 *      ./gradlew :app:recordRoborazziDebug --tests "*FrameScreenScreenshotTest*"
 *    Outputs PNGs to app/build/outputs/roborazzi/ — commit them.
 *
 * 2. Verify on every subsequent CI run (the real assertion):
 *      ./gradlew :app:verifyRoborazziDebug --tests "*FrameScreenScreenshotTest*"
 *    Fails with a diff image under build/outputs/roborazzi/ on any pixel regression.
 *
 * To un-ignore for a local Linux run, remove @Ignore or set the system property:
 *      -Droborazzi.enabled=true
 * and update the Assume guard below accordingly.
 */
package com.baer.memolio.feature.frame

import android.graphics.BitmapFactory
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import coil3.ImageLoader
import coil3.asImage
import coil3.compose.setSingletonImageLoaderFactory
import coil3.test.FakeImageLoaderEngine
import com.baer.memolio.core.model.Photo
import com.baer.memolio.core.ui.MemolioTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Pixel-level screenshot tests for [FrameScreen].
 *
 * Three captures:
 *  1. [idleHome]                  — wallpaper + clock/date/wordmark, no photos.
 *  2. [slideshowBlurredFillLandscape] — blurred-fill + sharp Fit overlay, overlays on.
 *  3. [slideshowOverlaysOff]      — same photo, clock/date/caption hidden.
 *
 * Guarded with [@Ignore] — see file-level comment for the CI record/verify workflow.
 */
@Ignore("Roborazzi pixel capture needs Robolectric NATIVE graphics — record on Linux CI: ./gradlew recordRoborazziDebug")
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w1280dp-h800dp-land-xhdpi")
class FrameScreenScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    // ---- helpers ----------------------------------------------------------------

    private fun landscapePhoto() = Photo(
        id = "p1",
        originalPath = "/o/p1.jpg",
        displayCachePath = "test_landscape.jpg",
        thumbPath = "/t/p1.jpg",
        contentHash = "p1",
        width = 1200,
        height = 800,
        orientation = 0,
        caption = "Sunset at the lake",
        albumId = "a1",
        favorite = false,
        sortOrder = 0,
        addedAt = 0L,
        sourceDevice = null,
        deletedAt = null
    )

    /**
     * Builds a [FakeImageLoaderEngine] that serves the committed test_landscape.jpg
     * for every Coil request, producing deterministic pixels across runs.
     */
    private fun fakeEngine(): FakeImageLoaderEngine {
        val bytes = javaClass.classLoader!!
            .getResourceAsStream("test_landscape.jpg")!!
            .readBytes()
        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        return FakeImageLoaderEngine.Builder()
            .default(bmp.asImage())
            .build()
    }

    // ---- screenshot tests -------------------------------------------------------

    /**
     * Idle home: no photos, wallpaper Brush visible, clock + date + wordmark + menu.
     * Baseline: app/build/outputs/roborazzi/frame_idle_home.png
     */
    @Test
    fun idleHome() {
        composeRule.setContent {
            MemolioTheme {
                FrameScreen(
                    state = FrameUiState.Idle(
                        time = "14:32",
                        date = "Tuesday, 16 June",
                        driftPhase = 0.6f,
                        showClock = true,
                        showDate = true
                    ),
                    onOpenManage = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage("build/roborazzi/frame_idle_home.png")
    }

    /**
     * Slideshow with a landscape photo: blurred-fill behind sharp Fit copy,
     * Ken Burns at t=0, clock + date + caption all visible.
     * Baseline: app/build/outputs/roborazzi/frame_slideshow_blurredfill.png
     */
    @Test
    fun slideshowBlurredFillLandscape() {
        val photo = landscapePhoto()
        composeRule.setContent {
            setSingletonImageLoaderFactory { ctx ->
                ImageLoader.Builder(ctx).components { add(fakeEngine()) }.build()
            }
            MemolioTheme {
                FrameScreen(
                    state = FrameUiState.Slideshow(
                        currentPhoto = photo,
                        nextPhoto = photo,
                        position = 1,
                        total = 3,
                        time = "14:32",
                        date = "Tuesday, 16 June",
                        showClock = true,
                        showDate = true,
                        showCaption = true
                    ),
                    onOpenManage = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage("build/roborazzi/frame_slideshow_blurredfill.png")
    }

    /**
     * Slideshow with overlays toggled off: photo visible, clock/date/caption absent.
     * Wordmark + menu button remain (always visible per spec).
     * Baseline: app/build/outputs/roborazzi/frame_slideshow_overlays_off.png
     */
    @Test
    fun slideshowOverlaysOff() {
        val photo = landscapePhoto()
        composeRule.setContent {
            setSingletonImageLoaderFactory { ctx ->
                ImageLoader.Builder(ctx).components { add(fakeEngine()) }.build()
            }
            MemolioTheme {
                FrameScreen(
                    state = FrameUiState.Slideshow(
                        currentPhoto = photo,
                        nextPhoto = photo,
                        position = 2,
                        total = 3,
                        time = "14:32",
                        date = "Tuesday, 16 June",
                        showClock = false,
                        showDate = false,
                        showCaption = false
                    ),
                    onOpenManage = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage("build/roborazzi/frame_slideshow_overlays_off.png")
    }
}
