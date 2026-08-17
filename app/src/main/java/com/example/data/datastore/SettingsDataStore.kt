package com.example.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "shipper_settings")

data class BankSettings(
    val bankName: String = "MB Bank (Ngân hàng Quân Đội)",
    val bankBin: String = "970422",
    val bankShortName: String = "MB",
    val accountNumber: String = "0988668899",
    val accountHolder: String = "NGUYEN VAN SHIPPER",
    val goongApiKey: String = "goong_map_key_speedy"
)

class SettingsDataStore(private val context: Context) {

    companion object {
        val KEY_BANK_NAME = stringPreferencesKey("bank_name")
        val KEY_BANK_BIN = stringPreferencesKey("bank_bin")
        val KEY_BANK_SHORT_NAME = stringPreferencesKey("bank_short_name")
        val KEY_ACCOUNT_NUMBER = stringPreferencesKey("account_number")
        val KEY_ACCOUNT_HOLDER = stringPreferencesKey("account_holder")
        val KEY_GOONG_API_KEY = stringPreferencesKey("goong_api_key")
    }

    val bankSettingsFlow: Flow<BankSettings> = context.dataStore.data.map { prefs ->
        BankSettings(
            bankName = prefs[KEY_BANK_NAME] ?: "MB Bank (Ngân hàng Quân Đội)",
            bankBin = prefs[KEY_BANK_BIN] ?: "970422",
            bankShortName = prefs[KEY_BANK_SHORT_NAME] ?: "MB",
            accountNumber = prefs[KEY_ACCOUNT_NUMBER] ?: "0988668899",
            accountHolder = prefs[KEY_ACCOUNT_HOLDER] ?: "NGUYEN VAN SHIPPER",
            goongApiKey = prefs[KEY_GOONG_API_KEY] ?: "goong_map_key_speedy"
        )
    }

    suspend fun saveBankSettings(
        bankName: String,
        bankBin: String,
        bankShortName: String,
        accountNumber: String,
        accountHolder: String,
        goongApiKey: String = ""
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BANK_NAME] = bankName
            prefs[KEY_BANK_BIN] = bankBin
            prefs[KEY_BANK_SHORT_NAME] = bankShortName
            prefs[KEY_ACCOUNT_NUMBER] = accountNumber
            prefs[KEY_ACCOUNT_HOLDER] = accountHolder.uppercase()
            if (goongApiKey.isNotBlank()) {
                prefs[KEY_GOONG_API_KEY] = goongApiKey
            }
        }
    }
}
