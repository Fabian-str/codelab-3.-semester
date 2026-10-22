package dat.routes;

import dat.config.ApplicationConfig;
import dat.config.HibernateConfig;
import dat.entities.Hotel;
import io.javalin.Javalin;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HotelEndpointTest {

    private static EntityManagerFactory emf;
    private static ApplicationConfig application;
    private static RequestSpecification requestSpec;

    private Hotel hotel;

    @BeforeAll
    static void beforeAll() {
        HibernateConfig.setTest(true);
        emf = HibernateConfig.getEntityManagerFactoryForTest();

        application = new ApplicationConfig();

        Javalin app = application
            .security()
            .route(new Routes().getRoutes())
            .cors()
            .exceptions()
            .apiExceptions()
            .start(0);

        requestSpec = new RequestSpecBuilder()
            .setBaseUri("http://localhost")
            .setPort(app.port())
            .setBasePath("/api")
            .build();
    }

    @BeforeEach
    void setUp() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            em.createQuery("DELETE FROM Room").executeUpdate();
            em.createQuery("DELETE FROM Hotel").executeUpdate();

            hotel = new Hotel(
                    "Bates Motel",
                    "123 Test Street",
                    Hotel.HotelType.BUDGET
            );

            em.persist(hotel);
            em.getTransaction().commit();
        }
    }

    @AfterAll
    static void afterAll() {
        try {
            if (application != null) {
                application.stopServer();
            }
        } finally {
            if (emf != null && emf.isOpen()) {
                emf.close();
            }

            HibernateConfig.setTest(false);
        }
    }

    @Test
    void readAll() {
        given()
            .spec(requestSpec)
            .when()
            .get("/hotels")
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", equalTo(1))
            .body("[0].id", equalTo(hotel.getId()))
            .body("[0].hotelName", equalTo("Bates Motel"))
            .body("[0].hotelAddress", equalTo("123 Test Street"))
            .body("[0].hotelType", equalTo("BUDGET"));
    }

    @Test
    void read() {
        given()
            .spec(requestSpec)
            .when()
            .get("/hotels/" + hotel.getId())
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", equalTo(hotel.getId()))
            .body("hotelName", equalTo("Bates Motel"))
            .body("hotelAddress", equalTo("123 Test Street"))
            .body("hotelType", equalTo("BUDGET"));
    }

    @Test
    void create() {
        String requestBody = """
            {
                "hotelName": "Hotel California",
                "hotelAddress": "California",
                "hotelType": "LUXURY"
            }
            """;

        Integer createdId = given()
            .spec(requestSpec)
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/hotels")
            .then()
            .log().ifValidationFails()
            .statusCode(201)
            .body("id", notNullValue())
            .body("hotelName", equalTo("Hotel California"))
            .body("hotelAddress", equalTo("California"))
            .body("hotelType", equalTo("LUXURY"))
            .extract()
            .path("id");

        given()
            .spec(requestSpec)
            .when()
            .get("/hotels/" + createdId)
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .body("id", equalTo(createdId))
            .body("hotelName", equalTo("Hotel California"));
    }

    @Test
    void update() {
        String requestBody = """
            {
                "hotelName": "Bates Hotel",
                "hotelAddress": "456 New Street",
                "hotelType": "STANDARD"
            }
            """;

        given()
            .spec(requestSpec)
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .put("/hotels/" + hotel.getId())
            .then()
            .log().ifValidationFails()
            .statusCode(200);

        given()
            .spec(requestSpec)
            .when()
            .get("/hotels/" + hotel.getId())
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .body("id", equalTo(hotel.getId()))
            .body("hotelName", equalTo("Bates Hotel"))
            .body("hotelAddress", equalTo("456 New Street"))
            .body("hotelType", equalTo("STANDARD"));
    }

    @Test
    void delete() {
        given()
            .spec(requestSpec)
            .when()
            .delete("/hotels/" + hotel.getId())
            .then()
            .log().ifValidationFails()
            .statusCode(204);

        try (EntityManager em = emf.createEntityManager()) {
            org.junit.jupiter.api.Assertions.assertNull(em.find(Hotel.class, hotel.getId()));
        }
    }

    @Test
    void populate() {
        given()
                .spec(requestSpec)
                .when()
                .get("/hotels/populate")
                .then()
                .log().ifValidationFails()
                .statusCode(200);

        try (EntityManager em = emf.createEntityManager()) {
            Long hotelCount = em.createQuery(
                    "SELECT COUNT(h) FROM Hotel h",
                    Long.class
            ).getSingleResult();

            Long roomCount = em.createQuery(
                    "SELECT COUNT(r) FROM Room r",
                    Long.class
            ).getSingleResult();

            assertEquals(3L, hotelCount.longValue());
            assertEquals(12L, roomCount.longValue());
        }
    }
}