package dat.daos.impl;

import dat.config.HibernateConfig;
import dat.dtos.RoomDTO;
import dat.entities.Hotel;
import dat.entities.Room;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class RoomDAOTest {

    private static EntityManagerFactory emf;
    private static RoomDAO roomDAO;

    private Hotel hotel;
    private Room room;

    @BeforeAll
    static void beforeAll() {
        emf = HibernateConfig.getEntityManagerFactoryForTest();
        roomDAO = RoomDAO.getInstance(emf);
    }

    @BeforeEach
    void setUp() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            em.createQuery("DELETE FROM Room").executeUpdate();
            em.createQuery("DELETE FROM Hotel").executeUpdate();

            hotel = new Hotel("Bates Motel", "123 Test Street", Hotel.HotelType.BUDGET);

            em.persist(hotel);

            room = new Room(101, new BigDecimal("800"), Room.RoomType.SINGLE);

            room.setHotel(hotel);
            em.persist(room);

            em.getTransaction().commit();
        }
    }

    @AfterAll
    static void afterAll() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    @Test
    void read() {
        RoomDTO actual = roomDAO.read(room.getRoomId());

        assertNotNull(actual);

        assertAll(
            () -> assertEquals(room.getRoomId(), actual.getId()),
            () -> assertEquals(101, actual.getRoomNumber()),
            () -> assertEquals(800, actual.getRoomPrice()),
            () -> assertEquals(Room.RoomType.SINGLE, actual.getRoomType())
        );
    }

    @Test
    void readAll() {
        var rooms = roomDAO.readAll();

        assertEquals(1, rooms.size());
        assertEquals(room.getRoomId(), rooms.get(0).getId());
    }

    @Test
    void addRoomToHotel() {
        Room newRoom = new Room(102, new BigDecimal("1200"), Room.RoomType.DOUBLE);

        roomDAO.addRoomToHotel(hotel.getId(), new RoomDTO(newRoom));

        try (EntityManager em = emf.createEntityManager()) {
            var savedRooms = em.createQuery(
                        """
                        SELECT r FROM Room r
                        WHERE r.hotel.id = :hotelId
                          AND r.roomNumber = :roomNumber
                        """,
                        Room.class
                )
                .setParameter("hotelId", hotel.getId())
                .setParameter("roomNumber", 102)
                .getResultList();

            assertEquals(1, savedRooms.size());

            Room saved = savedRooms.get(0);

            assertAll(
                () -> assertEquals(hotel.getId(), saved.getHotel().getId()),
                () -> assertEquals(0, new BigDecimal("1200").compareTo(saved.getRoomPrice())),
                () -> assertEquals(Room.RoomType.DOUBLE, saved.getRoomType())
            );
        }
    }

    @Test
    void update() {
        Room updatedRoom = new Room(103, new BigDecimal("1500"), Room.RoomType.SUITE);

        roomDAO.update(room.getRoomId(), new RoomDTO(updatedRoom));

        try (EntityManager em = emf.createEntityManager()) {
            Room saved = em.find(Room.class, room.getRoomId());

            assertNotNull(saved);

            assertAll(
                () -> assertEquals(103, saved.getRoomNumber()),
                () -> assertEquals(0, new BigDecimal("1500").compareTo(saved.getRoomPrice())),
                () -> assertEquals(Room.RoomType.SUITE, saved.getRoomType()),
                () -> assertEquals(hotel.getId(), saved.getHotel().getId())
            );
        }
    }

    @Test
    void delete() {
        roomDAO.delete(room.getRoomId());

        try (EntityManager em = emf.createEntityManager()) {
            assertNull(em.find(Room.class, room.getRoomId()));
            assertNotNull(em.find(Hotel.class, hotel.getId()));
        }
    }

    @Test
    void validatePrimaryKey_existingRoom() {
        assertTrue(roomDAO.validatePrimaryKey(room.getRoomId()));
    }

    @Test
    void validatePrimaryKey_missingRoom() {
        assertFalse(roomDAO.validatePrimaryKey(-1));
    }

    @Test
    void validateHotelRoomNumber_existingNumber() {
        assertTrue(roomDAO.validateHotelRoomNumber(101, hotel.getId()));
    }

    @Test
    void validateHotelRoomNumber_missingNumber() {
        assertFalse(roomDAO.validateHotelRoomNumber(999, hotel.getId()));
    }

    @Test
    void read_missingRoom() {
        assertNull(roomDAO.read(-1));
    }
}