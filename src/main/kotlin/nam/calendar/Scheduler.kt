@file:Suppress("UnstableApiUsage")

package nam.calendar

import com.google.common.collect.ImmutableRangeSet
import com.google.common.collect.Range
import java.time.ZonedDateTime

/**
 * Get availability intersection between this calendar and provided [calendars].
 */
fun getAvailability(
    calendars: List<Calendar>,
    timeRange: Range<ZonedDateTime>
): ImmutableRangeSet<ZonedDateTime> {
    return calendars.fold(ImmutableRangeSet.Builder<ZonedDateTime>().build())
    { acc: ImmutableRangeSet<ZonedDateTime>, cal: Calendar ->
        if (acc.isEmpty) return cal.getFreeRangeSet(timeRange)
        return acc.intersection(cal.getFreeRangeSet(timeRange))
    }
}