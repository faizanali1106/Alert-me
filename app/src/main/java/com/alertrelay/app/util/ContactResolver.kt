package com.alertrelay.app.util

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat

object ContactResolver {
    fun resolveDisplayName(context: Context, phoneNumber: String?): String? {
        if (phoneNumber.isNullOrBlank()) return null
        if (!hasContactsPermission(context)) return sanitizeNumber(phoneNumber)

        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
        return try {
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                    if (index >= 0) {
                        cursor.getString(index)?.trim()?.takeIf { it.isNotEmpty() }
                    } else null
                } else null
            } ?: sanitizeNumber(phoneNumber)
        } catch (_: Exception) {
            sanitizeNumber(phoneNumber)
        }
    }

    fun enrich(context: Context, raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val digits = raw.filter { it.isDigit() || it == '+' }
        return if (digits.length >= 7) {
            resolveDisplayName(context, raw) ?: raw.trim().take(80)
        } else {
            raw.trim().take(80)
        }
    }

    private fun sanitizeNumber(number: String): String {
        return number.trim().take(80)
    }

    private fun hasContactsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
