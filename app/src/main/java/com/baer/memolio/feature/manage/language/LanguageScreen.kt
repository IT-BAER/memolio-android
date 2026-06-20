package com.baer.memolio.feature.manage.language

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.baer.memolio.R
import com.baer.memolio.core.ui.component.DropdownItem
import com.baer.memolio.core.ui.component.MemolioDropdown
import com.baer.memolio.core.ui.component.SectionHead

private data class LangOption(val tag: String, val language: String, @StringRes val nameRes: Int)

/**
 * The 10 shipped languages, by BCP-47 tag + primary subtag (for matching the active
 * locale) + autonym. Order mirrors locales_config.xml / build.gradle localeFilters.
 */
private val LANGUAGES = listOf(
    LangOption("en-US", "en", R.string.lang_en),
    LangOption("de-DE", "de", R.string.lang_de),
    LangOption("fr-FR", "fr", R.string.lang_fr),
    LangOption("it-IT", "it", R.string.lang_it),
    LangOption("nl-NL", "nl", R.string.lang_nl),
    LangOption("pl-PL", "pl", R.string.lang_pl),
    LangOption("pt-PT", "pt", R.string.lang_pt),
    LangOption("es-ES", "es", R.string.lang_es),
    LangOption("ru-RU", "ru", R.string.lang_ru),
    LangOption("zh-CN", "zh", R.string.lang_zh),
)

/** Sentinel key for the "System default" option (clears the per-app locale override). */
private const val SYSTEM_KEY = "__system__"

/**
 * In-app language picker. A compact [MemolioDropdown] (the full list of 10 + "System default"
 * lives in the popup, not stacked down the page). Picking writes the per-app locale via
 * [AppCompatDelegate.setApplicationLocales] (backported to minSdk 26 by AppCompat, persisted by
 * the manifest autostore service); the delegate recreates the Activity so the whole UI
 * re-resolves resources in the chosen language. "System default" clears the override and follows
 * the device language. Free, no Pro gate.
 */
@Composable
fun LanguageScreen(modifier: Modifier = Modifier, scrollable: Boolean = true) {
    val current = AppCompatDelegate.getApplicationLocales()
    val currentLanguage = if (current.isEmpty) null else current[0]?.language
    val selectedKey =
        if (current.isEmpty) SYSTEM_KEY
        else LANGUAGES.firstOrNull { it.language == currentLanguage }?.tag ?: SYSTEM_KEY
    val scrollState = rememberScrollState()

    // Autonyms (中文, Русский, …) always render with the system font so the picker is
    // glyph-safe no matter what language the app is currently in (Inter has no CJK). The
    // translated "System default" label uses the brand font.
    val items = buildList {
        add(DropdownItem(SYSTEM_KEY, stringResource(R.string.language_system_default), systemFont = false))
        LANGUAGES.forEach { add(DropdownItem(it.tag, stringResource(it.nameRes), systemFont = true)) }
    }

    Column(modifier.then(if (scrollable) Modifier.verticalScroll(scrollState) else Modifier)) {
        SectionHead(
            title = stringResource(R.string.manage_section_language),
            sub = stringResource(R.string.language_subtitle),
        )
        MemolioDropdown(
            items = items,
            selectedKey = selectedKey,
            onSelect = { key -> applyLocale(if (key == SYSTEM_KEY) "" else key) },
            leadingIcon = "language",
            modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(),
        )
    }
}

private fun applyLocale(tag: String) {
    val locales =
        if (tag.isEmpty()) LocaleListCompat.getEmptyLocaleList()
        else LocaleListCompat.forLanguageTags(tag)
    AppCompatDelegate.setApplicationLocales(locales)
}
