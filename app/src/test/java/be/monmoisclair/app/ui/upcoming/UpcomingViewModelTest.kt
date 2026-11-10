package be.monmoisclair.app.ui.upcoming

import android.content.Context
import android.os.Looper
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import be.monmoisclair.app.data.db.AppDatabase
import be.monmoisclair.app.domain.usecase.ListUpcomingMonthsUseCase
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class UpcomingViewModelTest {

    @Test
    fun init_failure_unsticksLoading() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        db.close()

        val viewModel = UpcomingViewModel(ListUpcomingMonthsUseCase(db))
        shadowOf(Looper.getMainLooper()).idle()

        assertThat(viewModel.state.value.loading).isFalse()
        assertThat(viewModel.state.value.months).isEmpty()
    }
}
