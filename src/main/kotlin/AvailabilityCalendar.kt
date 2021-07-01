import com.google.common.collect.Range
import java.util.*

data class AvailabilityCalendar(val timezone: SimpleTimeZone, val busyRangeList: List<Range<Date>>, val workingHours: Range<Date>) {
    fun getFreeRangeList(): List<Range<Date>> {
        var freeRange: List<Range<Date>> = emptyList();
        // TODO add free range calculation
        return freeRange;
    }

    fun getAvailability(calendars: List<AvailabilityCalendar>): List<Range<Date>> {
        var availability: List<Range<Date>> = emptyList();
        for(calendar in calendars) {
            // TODO add availability calculation
        }
        return availability;
    }
}
