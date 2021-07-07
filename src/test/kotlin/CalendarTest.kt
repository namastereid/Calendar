@file:Suppress("UnstableApiUsage")

import com.google.common.collect.ImmutableRangeSet
import com.google.common.collect.Range
import nam.calendar.Calendar
import nam.calendar.getAvailability
import org.junit.jupiter.api.TestInstance
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CalendarTest {

    @Test
    fun `get free range`() {
        val busy =
            ImmutableRangeSet.Builder<LocalDateTime>()
                .add(
                    Range.closed(
                        LocalDateTime.parse("2020-07-01T08:30:00"),
                        LocalDateTime.parse("2020-07-01T08:55:00")
                    )
                )
                .add(
                    Range.closed(
                        LocalDateTime.parse("2020-07-01T10:00:00"),
                        LocalDateTime.parse("2020-07-01T12:30:00")
                    )
                )
                .add(
                    Range.closed(
                        LocalDateTime.parse("2020-07-03T09:00:00"),
                        LocalDateTime.parse("2020-07-03T09:30:00")
                    )
                )
                .build()
        val workingHours =
            Range.closed(
                LocalTime.parse("09:00"),
                LocalTime.parse("17:00")
            )
        val calendar = TestCalendar("user1", workingHours, ZoneId.of("America/New_York"), busy)

        val timeRange =
            Range.open(
                ZonedDateTime.parse("2020-07-01T10:00:00-06:00[America/Denver]"),
                ZonedDateTime.parse("2020-07-03T12:00:00-06:00[America/Denver]"),
            )
        val freeRangeSet = calendar.getFreeRangeSet(timeRange)

        val expected =
            ImmutableRangeSet.Builder<ZonedDateTime>()
                .add(
                    Range.open(
                        ZonedDateTime.parse("2020-07-01T10:30:00-06:00[America/Denver]"),
                        ZonedDateTime.parse("2020-07-01T15:00:00-06:00[America/Denver]")
                    )
                )
                .add(
                    Range.open(
                        ZonedDateTime.parse("2020-07-02T07:00:00-06:00[America/Denver]"),
                        ZonedDateTime.parse("2020-07-02T15:00:00-06:00[America/Denver]"),
                    )
                )
                .add(
                    Range.open(
                        ZonedDateTime.parse("2020-07-03T07:30:00-06:00[America/Denver]"),
                        ZonedDateTime.parse("2020-07-03T12:00:00-06:00[America/Denver]"),
                    )
                )
                .build()

        assertEquals(expected, freeRangeSet)
    }

    @Test
    fun `get availability between calendars with same schedule`() {
        val timeZone = ZoneId.of("America/Denver")
        val now = LocalDateTime.now()
        val busy = ImmutableRangeSet.Builder<LocalDateTime>()
            .add(Range.closed(now, now.plusHours(1)))
            .add(Range.closed(now.plusHours(2), now.plusHours(3)))
            .build()
        val workingHours = Range.closed(LocalTime.parse("00:00:00"), LocalTime.parse("23:59:59"))
        val calendar1 = TestCalendar("user1", workingHours, timeZone, busy)
        val calendar2 = TestCalendar("user2", workingHours, timeZone, busy)

        val timeRange =
            Range.open(
                LocalDateTime.parse("2020-07-01T00:00:00").atZone(timeZone),
                LocalDateTime.parse("2020-07-02T00:00:00").atZone(timeZone)
            )
        assertEquals(
            getAvailability(listOf(calendar1, calendar2), timeRange),
            calendar1.getFreeRangeSet(timeRange)
        )
    }

    @Test
    fun `get availability between calendars with different schedules`() {
        val timeZone = ZoneId.of("America/Denver")
        val workingHours = Range.closed(LocalTime.parse("09:00"), LocalTime.parse("17:00"))

        val busy1 = ImmutableRangeSet.Builder<LocalDateTime>()
            .add(Range.closed(LocalDateTime.parse("2020-07-01T08:30:00"), LocalDateTime.parse("2020-07-01T08:55:00")))
            .add(Range.closed(LocalDateTime.parse("2020-07-01T10:00:00"), LocalDateTime.parse("2020-07-01T12:00:00")))
            .build()

        val calendar1 = TestCalendar("user1", workingHours, timeZone, busy1)

        val busy2 = ImmutableRangeSet.Builder<LocalDateTime>()
            .add(Range.closed(LocalDateTime.parse("2020-07-01T09:00:00"), LocalDateTime.parse("2020-07-01T09:55:00")))
            .add(Range.closed(LocalDateTime.parse("2020-07-01T11:00:00"), LocalDateTime.parse("2020-07-01T14:00:00")))
            .build()

        val calendar2 = TestCalendar("user2", workingHours, timeZone, busy2)

        val expected = ImmutableRangeSet.Builder<ZonedDateTime>()
            .add(
                Range.open(
                    LocalDateTime.parse("2020-07-01T09:55").atZone(timeZone),
                    LocalDateTime.parse("2020-07-01T10:00").atZone(timeZone)
                )
            )
            .add(
                Range.open(
                    LocalDateTime.parse("2020-07-01T14:00").atZone(timeZone),
                    LocalDateTime.parse("2020-07-01T17:00").atZone(timeZone)
                )
            )
            .build()

        val timeRange =
            Range.open(
                LocalDateTime.parse("2020-07-01T00:00:00").atZone(timeZone),
                LocalDateTime.parse("2020-07-02T00:00:00").atZone(timeZone)
            )
        assertEquals(expected, getAvailability(listOf(calendar1, calendar2), timeRange))
    }

    @Test
    fun `get availability different timezone`() {
        val denverTZ = ZoneId.of("America/Denver")
        val newYorkTZ = ZoneId.of("America/New_York")

        val workingHours1 = Range.closed(LocalTime.parse("08:00"), LocalTime.parse("18:00"))

        val busy1 = ImmutableRangeSet.Builder<LocalDateTime>()
            .add(Range.closed(LocalDateTime.parse("2020-07-01T08:30:00"), LocalDateTime.parse("2020-07-01T08:55:00")))
            .add(Range.closed(LocalDateTime.parse("2020-07-01T10:00:00"), LocalDateTime.parse("2020-07-01T12:00:00")))
            .build()

        val calendar1 = TestCalendar("user1", workingHours1, denverTZ, busy1)

        val workingHours2 = Range.closed(LocalTime.parse("09:00"), LocalTime.parse("17:00"))
        val busy2 = ImmutableRangeSet.Builder<LocalDateTime>()
            .add(Range.closed(LocalDateTime.parse("2020-07-01T09:00:00"), LocalDateTime.parse("2020-07-01T09:55:00")))
            .add(Range.closed(LocalDateTime.parse("2020-07-01T11:00:00"), LocalDateTime.parse("2020-07-01T14:00:00")))
            .build()

        val calendar2 = TestCalendar("user2", workingHours2, newYorkTZ, busy2)

        val expected = ImmutableRangeSet.Builder<ZonedDateTime>()
            .add(
                Range.open(
                    LocalDateTime.parse("2020-07-01T08:00").atZone(denverTZ),
                    LocalDateTime.parse("2020-07-01T08:30").atZone(denverTZ)
                )
            )
            .add(
                Range.open(
                    LocalDateTime.parse("2020-07-01T08:55").atZone(denverTZ),
                    LocalDateTime.parse("2020-07-01T09:00").atZone(denverTZ)
                )
            )
            .add(
                Range.open(
                    LocalDateTime.parse("2020-07-01T12:00").atZone(denverTZ),
                    LocalDateTime.parse("2020-07-01T15:00").atZone(denverTZ)
                )
            )
            .build()

        val timeRange =
            Range.open(
                LocalDateTime.parse("2020-07-01T00:00:00").atZone(denverTZ),
                LocalDateTime.parse("2020-07-02T00:00:00").atZone(denverTZ)
            )
        assertEquals(expected, getAvailability(listOf(calendar1, calendar2), timeRange))
    }

    @Test
    fun `get availability different timezone afternoon only`() {
        val denverTZ = ZoneId.of("America/Denver")
        val newYorkTZ = ZoneId.of("America/New_York")

        val workingHours1 = Range.closed(LocalTime.parse("08:00"), LocalTime.parse("18:00"))

        val busy1 = ImmutableRangeSet.Builder<LocalDateTime>()
            .add(Range.closed(LocalDateTime.parse("2020-07-01T08:30:00"), LocalDateTime.parse("2020-07-01T08:55:00")))
            .add(Range.closed(LocalDateTime.parse("2020-07-01T10:00:00"), LocalDateTime.parse("2020-07-01T12:00:00")))
            .build()

        val calendar1 = TestCalendar("user1", workingHours1, denverTZ, busy1)

        val workingHours2 = Range.closed(LocalTime.parse("09:00"), LocalTime.parse("17:00"))
        val busy2 = ImmutableRangeSet.Builder<LocalDateTime>()
            .add(Range.closed(LocalDateTime.parse("2020-07-01T09:00:00"), LocalDateTime.parse("2020-07-01T09:55:00")))
            .add(Range.closed(LocalDateTime.parse("2020-07-01T11:00:00"), LocalDateTime.parse("2020-07-01T14:00:00")))
            .build()

        val calendar2 = TestCalendar("user2", workingHours2, newYorkTZ, busy2)

        val expected = ImmutableRangeSet.Builder<ZonedDateTime>()
            .add(
                Range.open(
                    LocalDateTime.parse("2020-07-01T13:00").atZone(denverTZ),
                    LocalDateTime.parse("2020-07-01T15:00").atZone(denverTZ)
                )
            )
            .build()

        val timeRange =
            Range.open(
                LocalDateTime.parse("2020-07-01T13:00:00").atZone(denverTZ),
                LocalDateTime.parse("2020-07-01T17:00:00").atZone(denverTZ)
            )
        assertEquals(expected, getAvailability(listOf(calendar1, calendar2), timeRange))
    }
}