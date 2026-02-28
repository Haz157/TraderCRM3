package apps.farm.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "auth_settings")

@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val IS_AUTH_ENABLED = booleanPreferencesKey("is_auth_enabled")
    private val USE_BIOMETRIC = booleanPreferencesKey("use_biometric")
    private val SAVED_PIN = stringPreferencesKey("saved_pin")
    private val BACKUP_EMAIL = stringPreferencesKey("backup_email")
    private val IS_AUTO_BACKUP_ENABLED = booleanPreferencesKey("is_auto_backup_enabled")

    val isAuthEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_AUTH_ENABLED] ?: false
    }

    val useBiometric: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USE_BIOMETRIC] ?: false
    }

    val savedPin: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[SAVED_PIN]
    }

    val backupEmail: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[BACKUP_EMAIL] ?: "htsolutionscodenest@gmail.com"
    }

    val isAutoBackupEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_AUTO_BACKUP_ENABLED] ?: false
    }

    suspend fun setAuthEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_AUTH_ENABLED] = enabled
        }
    }

    suspend fun setUseBiometric(use: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_BIOMETRIC] = use
        }
    }

    suspend fun setPin(pin: String) {
        context.dataStore.edit { preferences ->
            preferences[SAVED_PIN] = pin
        }
    }

    suspend fun setBackupEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[BACKUP_EMAIL] = email
        }
    }

    suspend fun setAutoBackupEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_AUTO_BACKUP_ENABLED] = enabled
        }
    }
}
