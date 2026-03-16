package com.japygo.modakmodak.utils

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.LocaleList
import java.util.Locale

class LocaleContextWrapper(base: Context) : ContextWrapper(base) {
    companion object {
        fun wrap(context: Context, language: String): ContextWrapper {
            var ctx = context
            val config = ctx.resources.configuration
            val sysLocale: Locale? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                config.locales.get(0)
            } else {
                @Suppress("DEPRECATION")
                config.locale
            }

            if (language.isNotEmpty() && !sysLocale?.language.equals(language)) {
                val locale = Locale(language)
                Locale.setDefault(locale)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    config.setLocale(locale)
                    val localeList = LocaleList(locale)
                    LocaleList.setDefault(localeList)
                    config.setLocales(localeList)
                } else {
                    @Suppress("DEPRECATION")
                    config.locale = locale
                }
                ctx = ctx.createConfigurationContext(config)
            }
            return LocaleContextWrapper(ctx)
        }
    }
}
