package be.monmoisclair.app.data.receipt

import android.graphics.Bitmap
import be.monmoisclair.app.domain.BelgianStructuredCommunication
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Lit seulement une communication structurée belge sur une photo.
 * Jamais un montant — le Reste reste sous contrôle manuel / QR.
 */
@Singleton
class ReceiptCommunicationOcr @Inject constructor() {

    suspend fun findStructuredCommunication(bitmap: Bitmap): String? =
        withContext(Dispatchers.Default) {
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            try {
                val image = InputImage.fromBitmap(bitmap, 0)
                val result = Tasks.await(recognizer.process(image), 20, TimeUnit.SECONDS)
                BelgianStructuredCommunication.findInOcrText(result.text.orEmpty())
            } finally {
                recognizer.close()
            }
        }
}
