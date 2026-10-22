package dat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dat.exceptions.ApiException;
import dat.routes.Routes;
import dat.utils.Utils;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.config.JavalinConfig;
import io.javalin.http.staticfiles.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.relation.Role;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ApplicationConfig {

    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);

    private final List<EndpointGroup> routes = new ArrayList<>();
    private final List<Consumer<JavalinConfig>> configSteps = new ArrayList<>();

    private Javalin app;

    public ApplicationConfig() {
        configSteps.add(this::applyBaseConfig);
    }

    public ApplicationConfig route(EndpointGroup route) {
        routes.add(route);
        return this;
    }

    public ApplicationConfig cors() {
        configSteps.add(config -> {
            config.bundledPlugins.enableCors(cors ->
                    cors.addRule(rule -> {
                        rule.anyHost();
                    })
            );
            config.bundledPlugins.enableHttpAllowedMethodsOnRoutes();
        });
        return this;
    }

    public ApplicationConfig security() {
        configSteps.add(config -> {
//            config.routes.beforeMatched(securityController::authenticate);
//            config.routes.beforeMatched(securityController::authorize);
        });
        return this;
    }

    public ApplicationConfig apiExceptions() {
        configSteps.add(config ->
                config.routes.exception(ApiException.class, (e, ctx) -> {
                    int statusCode = e.getStatusCode();
                    ObjectNode body = jsonMapper.createObjectNode()
                            .put("status", statusCode)
                            .put("msg", e.getMessage());
                    ctx.status(statusCode).json(body);
                })
        );
        return this;
    }

    public ApplicationConfig exceptions() {
        configSteps.add(config ->
                config.routes.exception(Exception.class, (e, ctx) -> {
                    ObjectNode body = jsonMapper.createObjectNode()
                            .put("status", 500)
                            .put("msg", "An unexpected error occurred");
                    logger.error("Unhandled exception", e);
                    ctx.status(500).json(body);
                })
        );
        return this;
    }

    public ApplicationConfig notFound() {
        configSteps.add(config ->
                config.routes.error(404, ctx -> {
                    String message = ctx.attribute("msg");
                    ObjectNode body = jsonMapper.createObjectNode()
                            .put("msg", message == null ? "Not found" : message);
                    ctx.json(body);
                })
        );
        return this;
    }

    public ApplicationConfig requestLogger() {
        configSteps.add(config ->
                config.routes.before(ctx ->
                        ctx.req().getHeaderNames().asIterator().forEachRemaining(System.out::println)
                )
        );
        return this;
    }

    public Javalin start(int port) {
        app = Javalin.create(config -> {
            for (Consumer<JavalinConfig> step : configSteps) {
                step.accept(config);
            }
            for (EndpointGroup route : routes) {
                config.routes.apiBuilder(route);
            }
        });

        app.start(port);
        return app;
    }

    public void stopServer() {
        if (app != null) {
            app.stop();
            app = null;
        }
    }

    private void applyBaseConfig(JavalinConfig config) {
        config.http.defaultContentType = "application/json";
        config.router.contextPath = "/api";

        config.bundledPlugins.enableDevLogging();
        config.bundledPlugins.enableRouteOverview("/routes");

        config.events.serverStarted(() ->
                System.out.println("Javalin started on http://localhost:7070/api")
        );

        config.events.serverStopped(() ->
                System.out.println("Javalin stopped")
        );
    }

}
