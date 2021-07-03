@file:Suppress("UnstableApiUsage")

import com.google.common.collect.ImmutableRangeSet
import com.google.common.collect.Range
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * [busyRangeSet] and [workingHours] are in local time. TimeZone will be used when [getAvailability] is called
 * to match availability between calendars.
 */
data class AvailabilityCalendar(
    val timeZone: ZoneId,
    val busyRangeSet: ImmutableRangeSet<ZonedDateTime>,
    val workingHours: Range<ZonedDateTime>
) {

    /**
     * Get [timeRange] with [zoneId] with the same instant.
     */
    private fun rangeWithZone(timeRange: Range<ZonedDateTime>, zoneId: ZoneId): Range<ZonedDateTime> {
        return Range.range(
            timeRange.lowerEndpoint().withZoneSameInstant(zoneId),
            timeRange.lowerBoundType(),
            timeRange.upperEndpoint().withZoneSameInstant(zoneId),
            timeRange.upperBoundType()
        )
    }

    /**
     * Get the range of free times within the [timeRange].
     */
    fun getFreeRangeSet(timeRange: Range<ZonedDateTime>): ImmutableRangeSet<ZonedDateTime> {
        val range = rangeWithZone(timeRange, timeZone)
        val start = range.lowerEndpoint()
        val end = range.upperEndpoint()

        val workingRangeSet =
            start.toLocalDate().datesUntil(end.toLocalDate()).toList()
                .fold(ImmutableRangeSet.Builder<ZonedDateTime>())
                { builder, date ->
                    builder.add(
                        Range.open(
                            date.atTime(workingHours.lowerEndpoint().toLocalTime()).atZone(timeZone),
                            date.atTime(workingHours.upperEndpoint().toLocalTime()).atZone(timeZone)
                        )
                    )
                }.build()

        return busyRangeSet.complement().subRangeSet(timeRange).intersection(workingRangeSet)
    }

    /**
     * Get availability intersection between this [AvailabilityCalendar] and provided [calendars].
     */
    fun getAvailability(
        calendars: List<AvailabilityCalendar>,
        timeRange: Range<ZonedDateTime>
    ): ImmutableRangeSet<ZonedDateTime> {
        //TODO handle timezones
        return calendars.fold(this.getFreeRangeSet(timeRange))
        { acc: ImmutableRangeSet<ZonedDateTime>, cal: AvailabilityCalendar ->
            return acc.intersection(cal.getFreeRangeSet(timeRange))
        }
    }
}
