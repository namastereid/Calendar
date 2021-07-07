@file:Suppress("UnstableApiUsage")

package nam.calendar.routes

import com.google.common.collect.Range
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import nam.calendar.Calendar
import nam.calendar.GoogleCalendar
import nam.calendar.getAvailability
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

fun Route.availabilityRoute() {
    route("/availability") {
        get {
            val userIds = call.request.queryParameters["id"]?.split(",")
            val timeMin = call.request.queryParameters["timeMin"]
            val timeMax = call.request.queryParameters["timeMax"]

            // construct availability calendar for the passed in users
            val timeZone = ZoneId.of("America/Denver")
            val workingHours = Range.closed(LocalTime.parse("09:00"), LocalTime.parse("17:00"))
            val range = Range.open(
                LocalDateTime.parse(timeMin).atZone(timeZone),
                LocalDateTime.parse(timeMax).atZone(timeZone)
            )
            val calendarList = userIds!!.fold(ArrayList<Calendar>()) { list, userId ->
                list.add(GoogleCalendar(userId, workingHours))
                list
            }

            call.respond(getAvailability(calendarList, range).toString())
        }
    }
}



fun Application.registerAvailabilityRoutes() {
    routing {
        availabilityRoute()
    }
}