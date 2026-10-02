package app.routes;

import app.config.ApplicationConfig;
import app.config.HibernateBaseProperties;
import app.config.HibernateEmfBuilder;
import app.controllers.PoemController;
import app.daos.PoemDAO;
import app.entities.Poem;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Properties;

import static org.hamcrest.Matchers.is;

public class PoemRoutesTest {

    private static PostgreSQLContainer<?> postgres;
    private static EntityManagerFactory emf;
    private static PoemDAO poemDAO;
    private static Javalin app;

    private int firstPoemId;

    @BeforeAll
    static void setup() {

        // Start isolated PostgreSQL test database
        postgres = new PostgreSQLContainer<>("postgres:16.2");
        postgres.start();

        // Hibernate properties for the test database
        Properties props = HibernateBaseProperties.createBase();

        props.put(
                "hibernate.connection.url",
                postgres.getJdbcUrl()
        );

        props.put(
                "hibernate.connection.username",
                postgres.getUsername()
        );

        props.put(
                "hibernate.connection.password",
                postgres.getPassword()
        );

        props.put(
                "hibernate.hbm2ddl.auto",
                "create-drop"
        );

        // Build Hibernate against the test database
        emf = HibernateEmfBuilder.build(props);

        poemDAO = new PoemDAO(emf);

        // Build application
        PoemController poemController =
                new PoemController(poemDAO);

        PoemRoutes poemRoutes =
                new PoemRoutes(poemController);

        Routes routes =
                new Routes(poemRoutes);

        ApplicationConfig applicationConfig =
                new ApplicationConfig(routes);

        app = applicationConfig.startServer(7070);

        // Rest Assured configuration
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 7070;
        RestAssured.basePath = "/api";
    }

    @BeforeEach
    void setupTestData() {

        // Clean database before each test
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Poem").executeUpdate();
            em.getTransaction().commit();
        }

        // Add known test data
        Poem first = poemDAO.create(new Poem(
                "First poem",
                "First poem text",
                "Haiku"
        ));

        firstPoemId = first.getId();

        poemDAO.create(new Poem(
                "Second poem",
                "Second poem text",
                "Free verse"
        ));
    }

    @AfterAll
    static void tearDown() {

        if (app != null) {
            app.stop();
        }

        if (emf != null && emf.isOpen()) {
            emf.close();
        }

        if (postgres != null) {
            postgres.stop();
        }
    }

    @Test
    void helloEndpointWorks() {

        RestAssured
                .given()
                .when()
                .get("/")
                .then()
                .statusCode(200)
                .body(is("Hello World"));
    }

    @Test
    void getAllPoems() {

        RestAssured
                .given()
                .when()
                .get("/poems")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].title", is("First poem"))
                .body("[1].title", is("Second poem"));
    }

    @Test
    void getPoemById() {

        RestAssured
                .given()
                .when()
                .get("/poems/" + firstPoemId)
                .then()
                .statusCode(200)
                .body("id", is(firstPoemId))
                .body("title", is("First poem"))
                .body("poem", is("First poem text"))
                .body("style", is("Haiku"));
    }

    @Test
    void getPoemByIdReturns404WhenNotFound() {

        RestAssured
                .given()
                .when()
                .get("/poems/99999")
                .then()
                .statusCode(404)
                .body("message", is("No poem found with id"))
                .body("id", is(99999));
    }

    @Test
    void createPoem() {

        String requestBody = """
                {
                  "title": "Created poem",
                  "poem": "Created through Rest Assured",
                  "style": "Haiku"
                }
                """;

        RestAssured
                .given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/poems")
                .then()
                .statusCode(201)
                .body("title", is("Created poem"))
                .body("poem", is("Created through Rest Assured"))
                .body("style", is("Haiku"));
    }

    @Test
    void updatePoem() {

        String requestBody = """
                {
                  "title": "Updated title",
                  "poem": "Updated poem text",
                  "style": "Free verse"
                }
                """;

        RestAssured
                .given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .put("/poems/" + firstPoemId)
                .then()
                .statusCode(200)
                .body("id", is(firstPoemId))
                .body("title", is("Updated title"))
                .body("poem", is("Updated poem text"))
                .body("style", is("Free verse"));
    }

    @Test
    void updatePoemReturns404WhenNotFound() {

        String requestBody = """
                {
                  "title": "Does not matter",
                  "poem": "This should not update anything",
                  "style": "Haiku"
                }
                """;

        RestAssured
                .given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .put("/poems/99999")
                .then()
                .statusCode(404)
                .body("message", is("No poem found with id"))
                .body("id", is(99999));
    }

    @Test
    void deletePoem() {

        RestAssured
                .given()
                .when()
                .delete("/poems/" + firstPoemId)
                .then()
                .statusCode(200)
                .body("message", is("Poem deleted"))
                .body("id", is(firstPoemId));
    }

    @Test
    void deletePoemReturns404WhenNotFound() {

        RestAssured
                .given()
                .when()
                .delete("/poems/99999")
                .then()
                .statusCode(404)
                .body("message", is("No poem found with id"))
                .body("id", is(99999));
    }

    @Test
    void createPoemBatch() {

        String requestBody = """
                [
                  {
                    "title": "Batch poem one",
                    "poem": "First batch poem",
                    "style": "Haiku"
                  },
                  {
                    "title": "Batch poem two",
                    "poem": "Second batch poem",
                    "style": "Free verse"
                  }
                ]
                """;

        RestAssured
                .given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/poems/batch")
                .then()
                .statusCode(201)
                .body("size()", is(2))
                .body("[0].title", is("Batch poem one"))
                .body("[1].title", is("Batch poem two"));
    }
}