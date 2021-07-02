import com.google.common.collect.Range
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

data class AvailabilityCalendar(val timezone: TimeZone, val busyRangeList: List<Range<LocalDateTime>>, val workingHours: Range<LocalTime>) {
    fun getFreeRangeList(timeRange: Range<LocalDateTime>): List<Range<LocalDateTime>> {
        var freeRange: List<Range<LocalDateTime>> = emptyList();
        // TODO add free range calculation
        return freeRange;
    }

    fun getAvailability(calendars: List<AvailabilityCalendar>, timeRange: Range<LocalDateTime>): List<Range<LocalDateTime>> {
        var availability: List<Range<LocalDateTime>> = emptyList();
        for(calendar in calendars) {
            // TODO add availability calculation
        }
        return availability;
    }
}
