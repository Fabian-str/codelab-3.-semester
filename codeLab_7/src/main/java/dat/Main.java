package dat;

import dat.config.ApplicationConfig;
import dat.routes.Routes;
import static io.javalin.apibuilder.ApiBuilder.*;

public class Main {

    public static void main(String[] args) {
        new ApplicationConfig()
                .security()
                .route(new Routes().getRoutes())
                .cors()
                .exceptions()
                .apiExceptions()
                .start(7070);
    }
}