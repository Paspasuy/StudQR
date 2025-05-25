package com.example.studqr

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import android.view.ContextThemeWrapper
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale

fun Context.setAppLocale(language: String): Context {
    val locale = Locale(language)
    Locale.setDefault(locale)

    val config = Configuration(resources.configuration)
    config.setLocale(locale)

    return createConfigurationContext(config)
}

fun setAppLocale2(languageFromPreference: String?, context: Context)
{

    if (languageFromPreference != null) {

        val resources: Resources = context.resources
        val dm: DisplayMetrics = resources.displayMetrics
        val config: Configuration = resources.configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(Locale(languageFromPreference.toLowerCase(Locale.ROOT)))
        } else {
            config.setLocale(Locale(languageFromPreference.toLowerCase(Locale.ROOT)))
        }
        resources.updateConfiguration(config, dm)
    }
}

class ApplicationLanguageHelper(base: Context) : ContextThemeWrapper(base, R.style.Base_Theme_StudQR) {
    companion object {

        fun wrap(context: Context, language: String): ContextThemeWrapper {
            var context = context
            val config = context.resources.configuration
            if (language != "") {
                val locale = Locale(language)
                Locale.setDefault(locale)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    setSystemLocale(config, locale)
                } else {
                    setSystemLocaleLegacy(config, locale)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    config.setLayoutDirection(locale)
                    context = context.createConfigurationContext(config)
                } else {
                    context.resources.updateConfiguration(config, context.resources.displayMetrics)
                }
            }
            return ApplicationLanguageHelper(context)
        }

        @SuppressWarnings("deprecation")
        fun setSystemLocaleLegacy(config: Configuration, locale: Locale) {
            config.locale = locale
        }

        @TargetApi(Build.VERSION_CODES.N)
        fun setSystemLocale(config: Configuration, locale: Locale) {
            config.setLocale(locale)
        }
    }
}
