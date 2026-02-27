package apps.farm

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale

@HiltAndroidApp
class FarmManagementApplication : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(setLocale(base, "ar"))
    }

    companion object {
        fun setLocale(context: Context, languageCode: String): Context {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)

            val config = Configuration(context.resources.configuration)
            config.setLocales(LocaleList(locale))

            return context.createConfigurationContext(config)
        }
    }
}
