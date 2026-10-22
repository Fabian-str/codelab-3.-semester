package dat.routes;

import dat.config.ApplicationConfig;
import dat.config.HibernateConfig;
import dat.entities.Hotel;
import dat.entities.Room;
import io.javalin.Javalin;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class RoomEndpointTest {

    private static EntityManagerFactory emf;
    private static ApplicationConfig application;
    private static RequestSpecification requestSpec;

    private Hotel hotel;
    private Room room;

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

            room = new Room(
                    101,
                    new BigDecimal("800"),
                    Room.RoomType.SINGLE
            );

            room.setHotel(hotel);
            em.persist(room);

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
            .get("/rooms")
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", equalTo(1))
            .body("[0].id", equalTo(room.getRoomId()))
            .body("[0].roomNumber", equalTo(101))
            .body("[0].roomPrice", equalTo(800))
            .body("[0].roomType", equalTo("SINGLE"));
    }

    @Test
    void read() {
        given()
            .spec(requestSpec)
            .when()
            .get("/rooms/" + room.getRoomId())
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", equalTo(room.getRoomId()))
            .body("roomNumber", equalTo(101))
            .body("roomPrice", equalTo(800))
            .body("roomType", equalTo("SINGLE"));
    }

    @Test
    void update() {
        String requestBody = """
            {
                "roomNumber": 102,
                "roomPrice": 1200,
                "roomType": "DOUBLE"
            }
            """;

        given()
            .spec(requestSpec)
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .put("/rooms/" + room.getRoomId())
            .then()
            .log().ifValidationFails()
            .statusCode(200);

        given()
            .spec(requestSpec)
            .when()
            .get("/rooms/" + room.getRoomId())
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .body("id", equalTo(room.getRoomId()))
            .body("roomNumber", equalTo(102))
            .body("roomPrice", equalTo(1200))
            .body("roomType", equalTo("DOUBLE"));
    }

    @Test
    void delete() {
        given()
            .spec(requestSpec)
            .when()
            .delete("/rooms/" + room.getRoomId())
            .then()
            .log().ifValidationFails()
            .statusCode(204);

        try (EntityManager em = emf.createEntityManager()) {
            assertNull(em.find(Room.class, room.getRoomId()));
            assertNotNull(em.find(Hotel.class, hotel.getId()));
        }
    }

    @Test
    void create() {
        Integer targetHotelId;

        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            Hotel targetHotel = new Hotel(
                    "Hotel California",
                    "California",
                    Hotel.HotelType.LUXURY
            );

            em.persist(targetHotel);
            em.getTransaction().commit();

            targetHotelId = targetHotel.getId();
        }

        String requestBody = """
            {
                "roomNumber": 201,
                "roomPrice": 1500,
                "roomType": "SUITE"
            }
            """;

        given()
            .spec(requestSpec)
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/rooms/hotel/" + targetHotelId)
            .then()
            .log().ifValidationFails()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("id", equalTo(targetHotelId))
            .body("rooms.size()", equalTo(1))
            .body("rooms[0].roomNumber", equalTo(201))
            .body("rooms[0].roomPrice", equalTo(1500))
            .body("rooms[0].roomType", equalTo("SUITE"));

        try (EntityManager em = emf.createEntityManager()) {
            var savedRooms = em.createQuery(
                        """
                        SELECT r FROM Room r
                        WHERE r.hotel.id = :hotelId
                          AND r.roomNumber = :roomNumber
                        """,
                        Room.class
                )
                .setParameter("hotelId", targetHotelId)
                .setParameter("roomNumber", 201)
                .getResultList();

            assertEquals(1, savedRooms.size());
            assertEquals(0, new BigDecimal("1500").compareTo(savedRooms.get(0).getRoomPrice()));
            assertEquals(Room.RoomType.SUITE, savedRooms.get(0).getRoomType());
        }
    }

    @Test
    void create_duplicateRoomNumber() {
        String requestBody = """
            {
                "roomNumber": 101,
                "roomPrice": 1200,
                "roomType": "DOUBLE"
            }
            """;

        given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/rooms/hotel/" + hotel.getId())
                .then()
                .log().ifValidationFails()
                .statusCode(400);

        // Det afviste kald må ikke have oprettet et ekstra værelse.
        try (EntityManager em = emf.createEntityManager()) {
            Long count = em.createQuery(
                            """
                            SELECT COUNT(r) FROM Room r
                            WHERE r.hotel.id = :hotelId
                            """,
                            Long.class
                    )
                    .setParameter("hotelId", hotel.getId())
                    .getSingleResult();

            assertEquals(1L, count.longValue());
        }
    }

    @Test
    void create_invalidRoomNumber() {
        String requestBody = """
            {
                "roomNumber": 0,
                "roomPrice": 1200,
                "roomType": "DOUBLE"
            }
            """;

        given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/rooms/hotel/" + hotel.getId())
                .then()
                .log().ifValidationFails()
                .statusCode(400);

        try (EntityManager em = emf.createEntityManager()) {
            Long count = em.createQuery("SELECT COUNT(r) FROM Room r", Long.class).getSingleResult();

            assertEquals(1L, count.longValue());
        }
    }

    @Test
    void create_missingHotel() {
        String requestBody = """
            {
                "roomNumber": 201,
                "roomPrice": 1200,
                "roomType": "DOUBLE"
            }
            """;

        given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/rooms/hotel/-1")
                .then()
                .log().ifValidationFails()
                .statusCode(400);

        try (EntityManager em = emf.createEntityManager()) {
            Long count = em.createQuery("SELECT COUNT(r) FROM Room r", Long.class).getSingleResult();

            assertEquals(1L, count.longValue());
        }
    }
}