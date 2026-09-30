package pac;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

/**
 * Requirement 12: PACFS UI - Banner Notifications
 * Covers setting a banner notification by an admin and viewing it by a regular user.
 */
public class TS_RefactorBannerNotificationsSimulation extends Simulation implements PacSimulation {

    // Feeder for banner messages (HTML format )
    private static final List<Map<String, Object>> bannerMessageData = Arrays.asList(
            Collections.singletonMap("bannerMessage", "<p><strong>Important:</strong> System maintenance tonight.</p>"),
            Collections.singletonMap("bannerMessage", "<p>New feature: <em>Case Notes</em> now available!</p>")
    );
    private static final FeederBuilder<Object> bannerMessageFeeder = listFeeder(bannerMessageData).random();

//    // Feeder for expiry time (random between 6 and 72 hours)
//    private static final FeederBuilder<Object> expiryTimeFeeder = new FeederBuilder.Source<Object>() {
//        @Override
//        public Iterator<Map<String, Object>> iterator() {
//            return new Iterator<Map<String, Object>>() {
//                @Override
//                public boolean hasNext() {
//                    return true; // Infinite feeder
//                }
//
//                @Override
//                public Map<String, Object> next() {
//                    int randomHours = ThreadLocalRandom.current().nextInt(6, 73); // 6 to 72 hours inclusive
//                    return Collections.singletonMap("expiryTime", String.valueOf(randomHours));
//                }
//            };
//        }
//    }.random();

    // Pre-generate the list of valid hours (6 to 72)
    private static final List<Map<String, Object>> expiryTimeData = new ArrayList<>();
    static {
        for (int i = 6; i <= 72; i++) {
            expiryTimeData.add(Collections.singletonMap("expiryTime", String.valueOf(i)));
        }
    }
    // Use the standard listFeeder
    private static final FeederBuilder<Object> expiryTimeFeeder = listFeeder(expiryTimeData).random();

    // --- Scenario 1: Admin Setting a Banner Notification ---

    // Access the /set-notification page
    private static final ChainBuilder accessSetNotificationPage = exec(
            http("Admin: Access Set Notification Page" )
                    .get("/set-notification")
                    .header("Authorization", "Bearer #{accessToken}") // Assuming admin uses same auth mechanism
                    .check(status().is(200))
                    .check(css("h1").is("Set Banner Notification")) // Assuming page title
    );

    // Submit the banner notification form
    private static final ChainBuilder submitBannerNotification = exec(
            http("Admin: Submit Banner Notification" )
                    .post("/set-notification")
                    .header("Authorization", "Bearer #{accessToken}")
                    .formParam("message", "#{bannerMessage}")
                    .formParam("expiry", "#{expiryTime}")
                    .check(status().is(200)) // Assuming 200 OK on successful save
                    .check(css(".govuk-notification-banner__heading").is("Banner notification saved")) // Assuming success message
    );

    // --- Scenario 2: User Seeing a Banner Notification ---

    // Access a common page (e.g., /my-courts) and check for the banner
    private static final ChainBuilder viewPageWithBanner = exec(
            http("User: View Page with Banner" )
                    .get("/my-courts") // Any page in the app
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
                    .check(css("#banner-notification").exists()) // Assuming the banner has an ID or class
    );

    // Admin scenario to set a banner
    private static final ScenarioBuilder adminSetBannerScenario = scenario("PACFS UI - Admin Set Banner")
            .exec(authenticate) // Admin authentication
            .pause(pause)
            .exec(accessSetNotificationPage)
            .pause(pause)
            .feed(bannerMessageFeeder)
            .feed(expiryTimeFeeder)
            .exec(submitBannerNotification);

    // User scenario to view a banner
    private static final ScenarioBuilder userViewBannerScenario = scenario("PACFS UI - User View Banner")
            .exec(authenticate) // Regular user authentication
            .pause(pause)
            .exec(viewPageWithBanner);

    {
        setUp(
                adminSetBannerScenario.injectOpen(atOnceUsers(1)), // Only one admin user to set the banner
                userViewBannerScenario.injectOpen(rampUsers(users).during(ramp)) // Multiple users viewing the banner
        ).protocols(httpProtocol );
    }
}
