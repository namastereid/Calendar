import com.google.common.collect.ImmutableRangeSet
import com.google.common.collect.Range
import nam.calendar.Calendar
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@Suppress("UnstableApiUsage")
class TestCalendar(
    userId: String,
    workingHours: Range<LocalTime>,
    override val timeZone: ZoneId,
    override val busyRangeSet: ImmutableRangeSet<LocalDateTime>
) : Calendar(userId, workingHours)