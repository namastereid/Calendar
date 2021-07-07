@file:Suppress("UnstableApiUsage")

import com.google.common.collect.ImmutableRangeSet
import com.google.common.collect.Range
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
}