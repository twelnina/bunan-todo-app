package dev.twelnina.bunantodo.ui.home

import dev.twelnina.bunantodo.data.time.CurrentDateProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

internal class FakeCurrentDateProvider(
    initialDate: LocalDate,
) : CurrentDateProvider {
    private val date = MutableStateFlow(initialDate)

    override fun observeDate(): Flow<LocalDate> = date.asStateFlow()

    fun changeDate(newDate: LocalDate) {
        date.value = newDate
    }
}