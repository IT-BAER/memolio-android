package com.baer.memolio.feature.manage.language

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.baer.memolio.R
import com.baer.memolio.core.ui.MemolioColors
import com.baer.memolio.core.ui.MemolioType
import com.baer.memolio.core.ui.Symbol
import com.baer.memolio.core.ui.component.CardVariant
import com.baer.memolio.core.ui.component.MemolioCard
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

/**
 * In-app language picker. Writes the per-app locale via [AppCompatDelegate.setApplicationLocales]
 * (backported to minSdk 26 by AppCompat, persisted by the manifest autostore service); the
 * delegate recreates the Activity so the whole UI re-resolves resources in the chosen language.
 * "System default" clears the override and follows the device language. Free, no Pro gate.
 */
@Composable
fun LanguageScreen(modifier: Modifier = Modifier) {
    val current = AppCompatDelegate.getApplicationLocales()
    val currentLanguage = if (current.isEmpty) null else current[0]?.language

    Column(modifier.verticalScroll(rememberScrollState())) {
        SectionHead(
            title = stringResource(R.string.manage_section_language),
            sub = stringResource(R.string.language_subtitle),
        )
        MemolioCard(
            modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(),
            variant = CardVariant.Surface,
        ) {
            Column {
                LanguageRow(
                    label = stringResource(R.string.language_system_default),
                    selected = current.isEmpty,
                    // The "System default" label is translated, so it uses the brand font.
                    autonym = false,
                    onClick = { applyLocale("") },
                )
                LANGUAGES.forEach { option ->
                    LanguageRow(
                        label = stringResource(option.nameRes),
                        selected = !current.isEmpty && currentLanguage == option.language,
                        autonym = true,
                        onClick = { applyLocale(option.tag) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageRow(
    label: String,
    selected: Boolean,
    autonym: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            color = if (selected) MemolioColors.TextPrimary else MemolioColors.TextSecondary,
            // Autonyms (中文, Русский, …) always render with the system font so the picker is
            // glyph-safe no matter what language the app is currently in (Inter has no CJK).
            style = if (autonym) MemolioType.body.copy(fontFamily = FontFamily.Default) else MemolioType.body,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
        )
        if (selected) Symbol("check", size = 22.sp, tint = MemolioColors.Teal)
    }
}

private fun applyLocale(tag: String) {
    val locales =
        if (tag.isEmpty()) LocaleListCompat.getEmptyLocaleList()
        else LocaleListCompat.forLanguageTags(tag)
    AppCompatDelegate.setApplicationLocales(locales)
}
