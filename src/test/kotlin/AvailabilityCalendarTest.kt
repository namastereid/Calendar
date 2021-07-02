@file:Suppress("UnstableApiUsage")

import com.google.common.collect.ImmutableRangeSet
import com.google.common.collect.Range
import org.junit.jupiter.api.TestInstance
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import kotlin.test.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AvailabilityCalendarTest {

    @Test
    fun `get free range`() {
        val timeZone = TimeZone.getTimeZone("America/Denver")
        val busy = ImmutableRangeSet.Builder<LocalDateTime>()
            .add(Range.closed(LocalDateTime.parse("2020-07-01T08:30:00"), LocalDateTime.parse("2020-07-01T08:55:00")))
            .add(Range.closed(LocalDateTime.parse("2020-07-01T10:00:00"), LocalDateTime.parse("2020-07-01T12:00:00")))
            .build()
        val workingHours = Range.closed(LocalTime.parse("09:00"), LocalTime.parse("17:00"))

        val calendar = AvailabilityCalendar(timeZone, busy, workingHours)
        val freeRangeSet = calendar.getFreeRangeSet(
            Range.open(
                LocalDateTime.parse("2020-07-01T00:00:00"),
                LocalDateTime.parse("2020-07-03T00:00:00")
            )
        )
        val expected = ImmutableRangeSet.Builder<LocalDateTime>()
            .add(Range.open(LocalDateTime.parse("2020-07-01T09:00:00"), LocalDateTime.parse("2020-07-01T10:00:00")))
            .add(Range.open(LocalDateTime.parse("2020-07-01T12:00:00"), LocalDateTime.parse("2020-07-01T17:00:00")))
            .add(Range.open(LocalDateTime.parse("2020-07-02T09:00:00"), LocalDateTime.parse("2020-07-02T17:00:00")))
            .build()

        assertEquals(expected, freeRangeSet)
    }

    @Test
    fun `get availability between calendars with same schedule`() {
        val timeZone = TimeZone.getTimeZone("America/Denver")
        val now = LocalDateTime.now()
        val busy = ImmutableRangeSet.Builder<LocalDateTime>()
            .add(Range.closed(now, now.plusHours(1)))
            .add(Range.closed(now.plusHours(2), now.plusHours(3)))
            .build()
        val workingHours = Range.closed(LocalTime.parse("00:00:00"), LocalTime.parse("23:59:59"))
        val calendar1 = AvailabilityCalendar(timeZone, busy, workingHours)
        val calendar2 = AvailabilityCalendar(timeZone, busy, workingHours)

        val timeRange =
            Range.open(LocalDateTime.parse("2020-07-01T00:00:00"), LocalDateTime.parse("2020-07-02T00:00:00"))
        assertEquals(
            calendar1.getAvailability(listOf(calendar2), timeRange),
            calendar1.getFreeRangeSet(timeRange)
        )
    }

    @Test
    fun `get availability between calendars with different schedules`() {
        val timeZone = TimeZone.getTimeZone("America/Denver")
        val workingHours = Range.closed(LocalTime.parse("09:00"), LocalTime.parse("17:00"))

        val busy1 = ImmutableRangeSet.Builder<LocalDateTime>()
            .add(Range.closed(LocalDateTime.parse("2020-07-01T08:30:00"), LocalDateTime.parse("2020-07-01T08:55:00")))
            .add(Range.closed(LocalDateTime.parse("2020-07-01T10:00:00"), LocalDateTime.parse("2020-07-01T12:00:00")))
            .build()

        val calendar1 = AvailabilityCalendar(timeZone, busy1, workingHours)

        val busy2 = ImmutableRangeSet.Builder<LocalDateTime>()
            .add(Range.closed(LocalDateTime.parse("2020-07-01T09:00:00"), LocalDateTime.parse("2020-07-01T09:55:00")))
            .add(Range.closed(LocalDateTime.parse("2020-07-01T11:00:00"), LocalDateTime.parse("2020-07-01T14:00:00")))
            .build()

        val calendar2 = AvailabilityCalendar(timeZone, busy2, workingHours)

        val expected = ImmutableRangeSet.Builder<LocalDateTime>()
            .add(Range.open(LocalDateTime.parse("2020-07-01T09:55"), LocalDateTime.parse("2020-07-01T10:00")))
            .add(Range.open(LocalDateTime.parse("2020-07-01T14:00"), LocalDateTime.parse("2020-07-01T17:00")))
            .build()

        val timeRange =
            Range.open(LocalDateTime.parse("2020-07-01T00:00:00"), LocalDateTime.parse("2020-07-02T00:00:00"))
        assertEquals(expected, calendar1.getAvailability(listOf(calendar2), timeRange))
    }
}