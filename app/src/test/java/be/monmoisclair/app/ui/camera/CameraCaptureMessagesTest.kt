package be.monmoisclair.app.ui.camera

import be.monmoisclair.app.ui.entry.EntryFormMessages
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CameraCaptureMessagesTest {

    @Test
    fun cancel_isNeutral_notPermission() {
        assertThat(CameraCaptureMessages.CAPTURE_CANCELLED).isEqualTo("Photo annulée.")
        assertThat(CameraCaptureMessages.CAPTURE_CANCELLED.lowercase())
            .doesNotContain("autorisation")
        assertThat(EntryFormMessages.CAMERA_CAPTURE_CANCELLED)
            .isEqualTo(CameraCaptureMessages.CAPTURE_CANCELLED)
    }

    @Test
    fun permissionTexts_reservedForGate() {
        assertThat(CameraCaptureMessages.NEED_PERMISSION.lowercase()).contains("autorisez")
        assertThat(CameraCaptureMessages.PERMANENTLY_DENIED.lowercase()).contains("réglages")
        assertThat(CameraCaptureMessages.OPEN_SETTINGS).isEqualTo("Ouvrir les réglages")
        assertThat(CameraCaptureMessages.CAPTURE_FAILED)
            .isNotEqualTo(CameraCaptureMessages.CAPTURE_CANCELLED)
        assertThat(CameraCaptureMessages.CAPTURE_FAILED.lowercase())
            .doesNotContain("autorisation")
        assertThat(CameraCaptureMessages.CAPTURE_FAILED.lowercase())
            .contains("réglages")
    }
}
