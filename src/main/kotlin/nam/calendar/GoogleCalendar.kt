package nam.calendar

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.store.DataStoreFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.FreeBusyRequest
import com.google.api.services.calendar.model.FreeBusyRequestItem
import com.google.api.services.calendar.model.TimePeriod
import com.google.common.collect.ImmutableRangeSet
import com.google.common.collect.Range
import java.io.*
import java.security.GeneralSecurityException
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Suppress("UnstableApiUsage")
class GoogleCalendar(userId: String, workingHours: Range<LocalTime>) : nam.calendar.Calendar(userId, workingHours) {
    private val applicationName = "Google Calendar API Java Quickstart"
    private val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()
    private val tokensDirectoryPath = "tokens"

    /**
     * Global instance of the scopes.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private val scopes = listOf(CalendarScopes.CALENDAR_READONLY)
    private val credentialsFilePath = "/credentials.json"

    private val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
    private val credentials = getCredentials()
    private val calendarService = Calendar.Builder(httpTransport, jsonFactory, credentials)
        .setApplicationName(applicationName)
        .build()

    private val calendar = calendarService.Calendars().get("primary").execute()
    private val primaryCalendarId = calendar.id
    private val freeBusy = getFreeBusy()

    override val timeZone: ZoneId
        get() = ZoneId.of(calendar.timeZone)

    override val busyRangeSet: ImmutableRangeSet<LocalDateTime>
        get() = freeBusy!!.fold(ImmutableRangeSet.Builder<LocalDateTime>()) { builder, timePeriod ->
            builder.add(
                Range.closed(
                    LocalDateTime.ofInstant(timePeriod?.start?.value?.let { Instant.ofEpochMilli(it) }, timeZone),
                    LocalDateTime.ofInstant(timePeriod?.end?.value?.let { Instant.ofEpochMilli(it) }, timeZone)
                )
            )
        }.build()


    /**
     * Creates an authorized Credential object.
     *
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    @Throws(IOException::class)
    private fun getCredentials(): Credential {
        // Load client secrets.
        val `in`: InputStream = GoogleCalendar::class.java.getResourceAsStream(credentialsFilePath)
            ?: throw FileNotFoundException("Resource not found: $credentialsFilePath")
        val clientSecrets = GoogleClientSecrets.load(jsonFactory, InputStreamReader(`in`))
        val dataStoreFactory: DataStoreFactory = FileDataStoreFactory(File(tokensDirectoryPath))

        // Build flow and trigger user authorization request.
        val flow = GoogleAuthorizationCodeFlow.Builder(
            httpTransport, jsonFactory, clientSecrets, scopes
        )
            .setDataStoreFactory(dataStoreFactory)
            .setAccessType("offline")
            .build()
        val receiver = LocalServerReceiver.Builder().setPort(8888).build()
        return AuthorizationCodeInstalledApp(flow, receiver).authorize(userId)
    }

    @Throws(IOException::class, GeneralSecurityException::class)
    private fun getFreeBusy(): List<TimePeriod?>? {
        val now = DateTime(System.currentTimeMillis())
        val dayInMs = (1000 * 60 * 60 * 24).toLong()
        val sevenDays = DateTime(System.currentTimeMillis() + dayInMs * 7)
        val items: MutableList<FreeBusyRequestItem> = ArrayList()
        items.add(FreeBusyRequestItem().setId(primaryCalendarId))
        return calendarService.freebusy().query(
            FreeBusyRequest()
                .setTimeMin(now)
                .setTimeMax(sevenDays)
                .setTimeZone("")
                .setItems(items)
        ).execute().calendars[primaryCalendarId]!!.busy
    }
}