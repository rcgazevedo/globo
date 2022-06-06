
import java.time.Duration;
import java.util.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import io.gatling.javaapi.jdbc.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import static io.gatling.javaapi.jdbc.JdbcDsl.*;

public class BBBSimulationLocal extends Simulation {

  {
    HttpProtocolBuilder httpProtocol = http;

    String uri1 = "http://localhost:8000/accounts/login/";

    String uri2 = "http://localhost:8000/polls/1/vote/";

    ScenarioBuilder scn = scenario("RecordedSimulation")

        .exec(
            http("login")
                .get(uri1)

        )
        .exec(getCookieValue(CookieKey("csrftoken")
            .withDomain("localhost")
            .withPath("/")
            .withSecure(false)))
        .exec(session -> {
          System.out.println(session.getString("csrftoken"));

          return session;
        })
        .exec(
            http("auth")
                .post(uri1)
                .formParam("csrfmiddlewaretoken", session -> session.getString("csrftoken"))
                // .formParam("csrfmiddlewaretoken", "furfles")
                .formParam("username", "admin")
                .formParam("password", "admin"))
        .exec(getCookieValue(CookieKey("sessionid")))
        .exec(session -> {
          System.out.println(session.getString("csrftoken"));
          System.out.println(session.getString("sessionid"));

          return session;
        })
        .exec(addCookie(Cookie("sessionid", session -> session.getString("sessionid"))
            .withDomain("localhost")
            .withPath("/")
            .withSecure(false)))
        .exec(addCookie(Cookie("csrftoken", session -> session.getString("csrftoken"))
            .withDomain("localhost")
            .withPath("/")
            .withSecure(false)))
        .exec(
            http("vote")
                .post(uri2)
                // .headers("Cookie",
                // "csrftoken=Xz0XxN6S0S51TVvSvCWJIVtlR45RLA6OK4eQLTnjkOYQGSiGJG81ZiyWDE1rQQc0;
                // sessionid=8zmfm45v0gdkden3drqyp339feo1m0ii")
                .formParam("csrfmiddlewaretoken", session -> session.getString("csrftoken"))
                .formParam("choice", "2")
                .formParam("passwg-recaptcha-response", ""));

    setUp(scn.injectOpen(
        constantUsersPerSec(10).during(10))).protocols(httpProtocol);
  }
}
