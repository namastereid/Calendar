@file:Suppress("UnstableApiUsage")

import com.google.common.collect.ImmutableRangeSet
import com.google.common.collect.Range
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAccessor
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
        // remove all date times that are outside working hours -- create a range for each one, complement it and intersect with timeRange
        val start = timeRange.lowerEndpoint()
        val end = timeRange.upperEndpoint()

        val builder = ImmutableRangeSet.Builder<LocalDateTime>()
        for (d in start.toLocalDate().datesUntil(end.toLocalDate())) {
            builder.add(Range.open(d.atTime(workingHours.lowerEndpoint()), d.atTime(workingHours.upperEndpoint())))
        }
        val workingRangeSet = builder.build();

        return busyRangeSet.complement().subRangeSet(timeRange).intersection(workingRangeSet)
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
