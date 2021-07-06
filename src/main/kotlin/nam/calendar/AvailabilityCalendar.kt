@file:Suppress("UnstableApiUsage")

package nam.calendar

import com.google.common.collect.ImmutableRangeSet
import com.google.common.collect.Range
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * [busyRangeSet] and [workingHours] are in local time. TimeZone will be used when [getAvailability] is called
 * to match availability between calendars.
 */
data class AvailabilityCalendar(
    val timeZone: ZoneId,
    val busyRangeSet: ImmutableRangeSet<LocalDateTime>,
    val workingHours: Range<LocalTime>
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
     * Set the [zoneId]] on a local [timeRange].
     */
    private fun localToZonedRange(timeRange: Range<LocalDateTime>, zoneId: ZoneId): Range<ZonedDateTime> {
        return Range.range(
            timeRange.lowerEndpoint().atZone(zoneId),
            timeRange.lowerBoundType(),
            timeRange.upperEndpoint().atZone(zoneId),
            timeRange.upperBoundType()
        )
    }

    /**
     * Get [Range] in [LocalDateTime] using [timeZone]
     */
    private fun rangeInLocal(timeRange: Range<ZonedDateTime>): Range<LocalDateTime> {
        return Range.range(
            timeRange.lowerEndpoint().withZoneSameInstant(timeZone).toLocalDateTime(),
            timeRange.lowerBoundType(),
            timeRange.upperEndpoint().withZoneSameInstant(timeZone).toLocalDateTime(),
            timeRange.upperBoundType()
        )
    }

    /**
     * Get the range of free times within the [timeRange].
     */
    fun getFreeRangeSet(timeRange: Range<ZonedDateTime>): ImmutableRangeSet<ZonedDateTime> {
        val rangeZoneId = timeRange.lowerEndpoint().zone
        val range = rangeInLocal(timeRange)
        val dateSequence = generateSequence(range.lowerEndpoint().toLocalDate()) { it.plusDays(1) }
        val workingRangeSet = dateSequence.takeWhile { it <= range.upperEndpoint().toLocalDate() }
            .fold(ImmutableRangeSet.Builder<LocalDateTime>())
            { builder, date ->
                builder.add(
                    Range.open(
                        date.atTime(workingHours.lowerEndpoint()),
                        date.atTime(workingHours.upperEndpoint())
                    )
                )
            }.build()

        val freeRangeSet = busyRangeSet.complement().subRangeSet(range).intersection(workingRangeSet)

        return freeRangeSet.asRanges().fold(ImmutableRangeSet.Builder<ZonedDateTime>())
        { builder, freeRange ->
            builder.add(rangeWithZone(localToZonedRange(freeRange, timeZone), rangeZoneId))
        }.build()
    }

    /**
     * Get availability intersection between this calendar and provided [calendars].
     */
    fun getAvailability(
        calendars: List<AvailabilityCalendar>,
        timeRange: Range<ZonedDateTime>
    ): ImmutableRangeSet<ZonedDateTime> {
        return calendars.fold(this.getFreeRangeSet(timeRange))
        { acc: ImmutableRangeSet<ZonedDateTime>, cal: AvailabilityCalendar ->
            return acc.intersection(cal.getFreeRangeSet(timeRange))
        }
    }
}
