package dev.twelnina.bunantodo.data.time

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import kotlin.time.Duration.Companion.seconds

class SystemCurrentDateProviderTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun observeDate_emitsCurrentDateOnSubscription() = runTest(timeout = 10.seconds) {
        val expected = LocalDate.of(2026, 9, 21)
        val provider = SystemCurrentDateProvider(
            context = context,
            currentDate = { expected }
        )

        assertEquals(expected, provider.observeDate().first())
    }

    @Test
    fun observeDate_readsDateAgainOnResubscription() = runTest(timeout = 10.seconds) {
        var today = LocalDate.of(2026, 9, 21)
        val provider = SystemCurrentDateProvider(
            context = context,
            currentDate = { today }
        )

        val dates = provider.observeDate()

        assertEquals(today, dates.first())

        today = today.plusDays(1)

        assertEquals(today, dates.first())
    }

}
