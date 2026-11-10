package be.monmoisclair.app.ui.dossiers

import be.monmoisclair.app.ui.camera.CameraCaptureMessages
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PendingLetterFormScreenTest {

    @Test
    fun failedCameraResult_usesFailureCopy_notCancelledCopy() {
        val msg = pendingLetterCameraFailureMessage(saved = false)

        assertThat(msg).isEqualTo(CameraCaptureMessages.CAPTURE_FAILED)
        assertThat(msg).isNotEqualTo(CameraCaptureMessages.CAPTURE_CANCELLED)
    }

    @Test
    fun savedCameraResult_hasNoErrorCopy() {
        assertThat(pendingLetterCameraFailureMessage(saved = true)).isNull()
    }
}
