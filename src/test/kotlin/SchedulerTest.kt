import com.google.common.collect.ImmutableRangeSet
import com.google.common.collect.Range
import nam.calendar.getAvailability
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.*;

@Suppress("UnstableApiUsage")
class SchedulerTest {
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
