package be.monmoisclair.app.data.backup

import java.security.MessageDigest

object BackupChecksum {

    fun sha256Hex(utf8Payload: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(utf8Payload.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { byte -> "%02x".format(byte) }
    }

    fun matches(expectedHex: String, utf8Payload: String): Boolean {
        return expectedHex.equals(sha256Hex(utf8Payload), ignoreCase = true)
    }
}
