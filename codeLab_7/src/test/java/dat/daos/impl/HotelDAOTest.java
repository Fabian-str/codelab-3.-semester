package dat.daos.impl;

import dat.config.HibernateConfig;
import dat.dtos.HotelDTO;
import dat.entities.Hotel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class HotelDAOTest {

    private static EntityManagerFactory emf;
    private static HotelDAO hotelDAO;

    private Hotel hotel;

    @BeforeAll
    static void beforeAll() {
        emf = HibernateConfig.getEntityManagerFactoryForTest();
        hotelDAO = HotelDAO.getInstance(emf);
    }

    @BeforeEach
    void setUp() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            // Værelser slettes først på grund af relationen til hoteller.
            em.createQuery("DELETE FROM Room").executeUpdate();
            em.createQuery("DELETE FROM Hotel").executeUpdate();

            hotel = new Hotel("Bates Motel", "123 Test Street", Hotel.HotelType.BUDGET);

            em.persist(hotel);
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
        HotelDTO actual = hotelDAO.read(hotel.getId());

        assertAll(
                () -> assertEquals(hotel.getId(), actual.getId()),
                () -> assertEquals("Bates Motel", actual.getHotelName()),
                () -> assertEquals("123 Test Street", actual.getHotelAddress()),
                () -> assertEquals(Hotel.HotelType.BUDGET, actual.getHotelType())
        );
    }

    @Test
    void create() {
        Hotel newHotel = new Hotel("Hotel California", "California", Hotel.HotelType.LUXURY);

        HotelDTO created = hotelDAO.create(new HotelDTO(newHotel));

        assertNotNull(created.getId());

        // Kontrollér databasen gennem en ny EntityManager.
        try (EntityManager em = emf.createEntityManager()) {
            Hotel saved = em.find(Hotel.class, created.getId());

            assertNotNull(saved);

            assertAll(
                    () -> assertEquals("Hotel California", saved.getHotelName()),
                    () -> assertEquals("California", saved.getHotelAddress()),
                    () -> assertEquals(Hotel.HotelType.LUXURY, saved.getHotelType())
            );
        }
    }

    @Test
    void readAll() {
        var hotels = hotelDAO.readAll();

        assertEquals(1, hotels.size());
        assertEquals(hotel.getId(), hotels.get(0).getId());
    }

    @Test
    void update() {
        Hotel updatedHotel = new Hotel("Bates Hotel", "456 New Street", Hotel.HotelType.STANDARD);

        hotelDAO.update(hotel.getId(), new HotelDTO(updatedHotel));

        // Kontrollér de gemte værdier gennem en ny EntityManager.
        try (EntityManager em = emf.createEntityManager()) {
            Hotel saved = em.find(Hotel.class, hotel.getId());

            assertNotNull(saved);

            assertAll(
                    () -> assertEquals("Bates Hotel", saved.getHotelName()),
                    () -> assertEquals("456 New Street", saved.getHotelAddress()),
                    () -> assertEquals(Hotel.HotelType.STANDARD, saved.getHotelType())
            );
        }
    }

    @Test
    void delete() {
        hotelDAO.delete(hotel.getId());

        try (EntityManager em = emf.createEntityManager()) {
            assertNull(em.find(Hotel.class, hotel.getId()));
        }
    }

    @Test
    void validatePrimaryKey_existingHotel() {
        assertTrue(hotelDAO.validatePrimaryKey(hotel.getId()));
    }

    @Test
    void validatePrimaryKey_missingHotel() {
        assertFalse(hotelDAO.validatePrimaryKey(-1));
    }
}