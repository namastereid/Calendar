import com.google.common.collect.Range
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDateTime
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AvailabilityCalendarTest {

    @Disabled
    @Test
    fun getAvailability() {
        val timeZone = TimeZone.getTimeZone("America/Denver")
        val now = LocalDateTime.now()
        val busy = listOf(
            Range.closed(now, now.plusHours(1)),
            Range.closed(now.plusHours(2), now.plusHours(3))
        )
//        val calendar = AvailabilityCalendar(SimpleTimeZone(0, "UTC"), )
    }
}