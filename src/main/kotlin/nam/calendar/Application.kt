package nam.calendar

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.serialization.*
import nam.calendar.routes.registerAvailabilityRoutes

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module(testing: Boolean = false) {
    install(ContentNegotiation) {
        json()
    }

    registerAvailabilityRoutes();
}