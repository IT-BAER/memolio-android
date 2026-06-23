package com.baer.memolio.feature.frame

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.random.Random

class FramePlaylistTest {

    private fun ids(n: Int) = (1..n).map { "p$it" }

    @Test
    fun orderedPlaylistKeepsInputOrder() {
        val pl = FramePlaylist.create(ids(3), shuffle = false, seed = 0L)
        assertThat(pl.order).containsExactly("p1", "p2", "p3").inOrder()
    }

    @Test
    fun shuffledPlaylistIsPermutationOfInput() {
        val pl = FramePlaylist.create(ids(5), shuffle = true, seed = 42L)
        assertThat(pl.order).containsExactlyElementsIn(ids(5))
    }

    @Test
    fun shuffleIsDeterministicForSameSeed() {
        val a = FramePlaylist.create(ids(8), shuffle = true, seed = 7L).order
        val b = FramePlaylist.create(ids(8), shuffle = true, seed = 7L).order
        assertThat(a).isEqualTo(b)
    }

    @Test
    fun advanceWrapsAroundAtEnd() {
        val pl = FramePlaylist.create(ids(3), shuffle = false, seed = 0L)
        assertThat(pl.currentId).isEqualTo("p1")
        assertThat(pl.nextId).isEqualTo("p2")
        val one = pl.advanced()
        assertThat(one.currentId).isEqualTo("p2")
        val two = one.advanced()
        assertThat(two.currentId).isEqualTo("p3")
        assertThat(two.nextId).isEqualTo("p1") // peek wraps
        val three = two.advanced()
        assertThat(three.currentId).isEqualTo("p1") // index wraps
    }

    @Test
    fun emptyPlaylistHasNoCurrentOrNext() {
        val pl = FramePlaylist.create(emptyList(), shuffle = false, seed = 0L)
        assertThat(pl.currentId).isNull()
        assertThat(pl.nextId).isNull()
        assertThat(pl.advanced().currentId).isNull()
    }

    @Test
    fun singlePhotoNextIsItself() {
        val pl = FramePlaylist.create(listOf("only"), shuffle = false, seed = 0L)
        assertThat(pl.currentId).isEqualTo("only")
        assertThat(pl.nextId).isEqualTo("only")
        assertThat(pl.advanced().currentId).isEqualTo("only")
    }

    @Test
    fun indexInfoIsOneBasedHumanReadable() {
        val pl = FramePlaylist.create(ids(4), shuffle = false, seed = 0L).advanced()
        assertThat(pl.position).isEqualTo(2)
        assertThat(pl.size).isEqualTo(4)
    }

    @Test
    fun randomSeedConstructorMatchesExplicitSeed() {
        val seed = Random(99L).nextLong()
        val a = FramePlaylist.create(ids(6), shuffle = true, seed = seed).order
        val b = FramePlaylist.create(ids(6), shuffle = true, seed = seed).order
        assertThat(a).isEqualTo(b)
    }

    // ---- rewound() ---------------------------------------------------------------

    @Test
    fun rewindStepsIndexBack() {
        val pl = FramePlaylist.create(ids(3), shuffle = false, seed = 0L).advanced().advanced()
        // index is now 2 (p3)
        assertThat(pl.currentId).isEqualTo("p3")
        val rw = pl.rewound()
        assertThat(rw.currentId).isEqualTo("p2")
    }

    @Test
    fun rewindWrapsFromZeroToLast() {
        val pl = FramePlaylist.create(ids(3), shuffle = false, seed = 0L)
        assertThat(pl.currentId).isEqualTo("p1")
        val rw = pl.rewound()
        assertThat(rw.currentId).isEqualTo("p3")
    }

    @Test
    fun rewindOnEmptyIsNoOp() {
        val pl = FramePlaylist.create(emptyList(), shuffle = false, seed = 0L)
        assertThat(pl.rewound().currentId).isNull()
    }

    @Test
    fun rewindOnSinglePhotoStaysSame() {
        val pl = FramePlaylist.create(listOf("only"), shuffle = false, seed = 0L)
        assertThat(pl.rewound().currentId).isEqualTo("only")
    }
}
