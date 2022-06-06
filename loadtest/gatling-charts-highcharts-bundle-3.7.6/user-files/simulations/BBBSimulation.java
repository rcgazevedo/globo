
import java.time.Duration;
import java.util.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import io.gatling.javaapi.jdbc.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import static io.gatling.javaapi.jdbc.JdbcDsl.*;

public class BBBSimulation extends Simulation {

  {
    HttpProtocolBuilder httpProtocol = http;

    String uri1 = "http://34.73.7.78/accounts/login/";

    String uri2 = "http://34.73.7.78/polls/1/vote/";

    ScenarioBuilder scn = scenario("RecordedSimulation")

        .exec(
            http("login")
                .get(uri1)

        )
        .exec(getCookieValue(CookieKey("csrftoken")
            .withDomain("34.73.7.78")
            .withPath("/")
            .withSecure(false)))
        .exec(session -> {
          // System.out.println(session.getString("csrftoken"));

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
          // System.out.println(session.getString("csrftoken"));
          // System.out.println(session.getString("sessionid"));

          return session;
        })
        .exec(addCookie(Cookie("sessionid", session -> session.getString("sessionid"))
            .withDomain("34.73.7.78")
            .withPath("/")
            .withSecure(false)))
        .exec(addCookie(Cookie("csrftoken", session -> session.getString("csrftoken"))
            .withDomain("34.73.7.78")
            .withPath("/")
            .withSecure(false)))
        .exec(
            http("vote")
                .post(uri2)
                .formParam("csrfmiddlewaretoken", session -> session.getString("csrftoken"))
                .formParam("choice", "2")
                .formParam("passwg-recaptcha-response", ""));

    setUp(scn.injectOpen(
        constantUsersPerSec(2).during(10))).protocols(httpProtocol);
  }
}
