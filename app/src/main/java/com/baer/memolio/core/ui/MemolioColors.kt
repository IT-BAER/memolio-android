package com.baer.memolio.core.ui

import androidx.compose.ui.graphics.Color

/**
 * Memolio color tokens — the full design-system palette, ported 1:1 from the
 * `:root` custom properties in docs/design/Memolio Frame.html (which itself was
 * ported from this app's earlier MemolioColors + the home-wallpaper mockup).
 *
 * Warm, dark, appliance-grade: a deep near-black warm base, a single warm
 * near-white "paper" ink, two restrained ambient accents (rosy clay + honey
 * gold), and semantic status. Everything below references these.
 *
 * The historical top-level names (MemolioInk, MemolioBackground, …) are kept
 * for source/test compatibility; the grouped [MemolioColors] object is the
 * canonical surface for new code.
 */
object MemolioColors {

    // ---- Base neutrals (warm dark) — gradient stops of the frame wallpaper ----
    val Ink000 = Color(0xFF07080C) // deepest frame background (darkest gradient stop)
    val Ink050 = Color(0xFF0A0C11) // near-black surface (rail)
    val Ink100 = Color(0xFF10151C) // primary surface / card
    val Ink150 = Color(0xFF161B22) // raised surface
    val Ink200 = Color(0xFF1C212B) // hairline-lifted surface
    val Ink300 = Color(0xFF221B27) // warm plum midtone (wallpaper accent stop)
    val Ink350 = Color(0xFF100E0D) // warm-brown terminal stop

    // ---- Warm near-white "paper" ink (the only light value) ----
    val Paper = Color(0xFFF4F1EA)
    val Paper680 = Paper.copy(alpha = 0.68f) // secondary text
    val Paper420 = Paper.copy(alpha = 0.42f) // quiet rules
    val Paper340 = Paper.copy(alpha = 0.34f) // wordmark
    val Paper280 = Paper.copy(alpha = 0.28f) // tertiary
    val Paper120 = Paper.copy(alpha = 0.12f) // disabled

    // ---- Glass / hairline (white at very low alpha) ----
    val Glass035 = Color.White.copy(alpha = 0.035f) // panel fill
    val Glass060 = Color.White.copy(alpha = 0.06f) // raised panel fill
    val Glass075 = Color.White.copy(alpha = 0.075f) // strong glass
    val Hairline = Color.White.copy(alpha = 0.10f) // default stroke
    val HairlineSoft = Color.White.copy(alpha = 0.07f) // faint divider

    // ---- Accents — rosy clay (primary). Names kept (Teal*) for back-compat. ----
    val Teal = Color(0xFFC4746A)
    val TealSoft = Color(0xFFD68F86)
    val TealDeep = Color(0xFF9D5249)
    val TealVivid = Color(0xFFD6816F)
    val TealWash = Color(0xFFC4746A).copy(alpha = 0.12f)

    // ---- Accents — honey gold (secondary). Names kept (Amber*) for back-compat. ----
    val Amber = Color(0xFFC6A968)
    val AmberSoft = Color(0xFFD8C08C)
    val AmberDeep = Color(0xFF997D44)
    val AmberVivid = Color(0xFFDDB86B)
    val AmberWash = Color(0xFFC6A968).copy(alpha = 0.12f)

    // ---- Semantic status ----
    val Ok = Color(0xFF54D18C)
    val Warn = Color(0xFFE0B15E)
    val Error = Color(0xFFE0796B)

    // ============================================================
    // Semantic aliases — reference these in components.
    // ============================================================
    val BgApp = Ink000
    val BgSurface = Ink100
    val BgRaised = Ink150
    val BgOverlay = Ink050

    val SurfaceCard = Ink100
    val SurfaceCardRaised = Ink150
    val SurfaceGlass = Glass035
    val SurfaceGlassStrong = Glass075

    val TextPrimary = Paper
    val TextSecondary = Paper680
    val TextTertiary = Paper280
    val TextDisabled = Paper120
    val TextOnAccent = Ink000

    val BorderDefault = Hairline
    val BorderSoft = HairlineSoft

    val Accent = Teal
    val AccentHover = TealSoft
    val AccentPress = TealDeep
    val Accent2 = Amber
    val Accent2Hover = AmberSoft
    val Accent2Press = AmberDeep

    val FocusRing = Color(0xFFC4746A).copy(alpha = 0.55f)
}

// ---- Legacy top-level aliases (kept for existing overlays + tests) ----

/** `--paper`/`--ink`: #f4f1ea — warm near-white for clock + primary text. */
val MemolioInk = MemolioColors.Paper

/** `--muted`: paper at 68% — secondary text (date). */
val MemolioMuted = MemolioColors.Paper680

/** `--faint`: paper at 28%; the wordmark uses 34%. */
val MemolioFaint = MemolioColors.Paper280
val MemolioWordmarkColor = MemolioColors.Paper340

/** `--glass`: subtle translucent panel fill for the menu button. */
val MemolioGlass = MemolioColors.Glass035
val MemolioGlassStroke = MemolioColors.Hairline

/** Deep frame background (#07080c) + primary surface. */
val MemolioBackground = MemolioColors.Ink000
val MemolioSurface = MemolioColors.Ink100

/** Ambient accents. */
val MemolioAccentTeal = MemolioColors.Teal
val MemolioAccentAmber = MemolioColors.Amber
