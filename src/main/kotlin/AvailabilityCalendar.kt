@file:Suppress("UnstableApiUsage")

import com.google.common.collect.ImmutableRangeSet
import com.google.common.collect.Range
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

data class AvailabilityCalendar(
    val timezone: TimeZone,
    val busyRangeSet: ImmutableRangeSet<LocalDateTime>,
    val workingHours: Range<LocalTime>
) {

    /**
     * Get the range of free times within the [timeRange].
     */
    fun getFreeRangeSet(timeRange: Range<LocalDateTime>): ImmutableRangeSet<LocalDateTime>? {
        return busyRangeSet.complement().subRangeSet(timeRange)
    }

    /**
     * Get availability intersection between this [AvailabilityCalendar] and provided [calendars].
     */
    fun getAvailability(
        calendars: List<AvailabilityCalendar>,
        timeRange: Range<LocalDateTime>
    ): List<Range<LocalDateTime>> {
        var availability: List<Range<LocalDateTime>> = emptyList();
        for (calendar in calendars) {
            // TODO add availability calculation
        }
        return availability;
    }
}
