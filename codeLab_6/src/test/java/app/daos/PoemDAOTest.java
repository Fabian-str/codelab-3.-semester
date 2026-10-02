package app.daos;

import app.config.HibernateEmfBuilder;
import app.config.HibernateBaseProperties;
import app.entities.Poem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PoemDAOTest {

    private static PostgreSQLContainer<?> postgres;
    private static EntityManagerFactory emf;
    private static PoemDAO poemDAO;

    @BeforeAll
    static void setup() {

        postgres = new PostgreSQLContainer<>("postgres:16.2");
        postgres.start();

        Properties props = HibernateBaseProperties.createBase();

        props.put("hibernate.connection.url", postgres.getJdbcUrl());
        props.put("hibernate.connection.username", postgres.getUsername());
        props.put("hibernate.connection.password", postgres.getPassword());

        props.put("hibernate.hbm2ddl.auto", "create-drop");

        emf = HibernateEmfBuilder.build(props);

        poemDAO = new PoemDAO(emf);
    }

    @BeforeEach
    void cleanDatabase() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Poem").executeUpdate();
            em.getTransaction().commit();
        }
    }

    @AfterAll
    static void tearDown() {

        if (emf != null) {
            emf.close();
        }

        if (postgres != null) {
            postgres.stop();
        }
    }

    @Test
    void createPoem() {

        Poem poem = new Poem(
                "Test poem",
                "This is a test poem",
                "Haiku"
        );

        Poem created = poemDAO.create(poem);

        assertThat(created.getId(), is(notNullValue()));
        assertThat(created.getTitle(), is("Test poem"));
    }

    @Test
    void getPoemById() {

        Poem poem = new Poem(
                "Find me",
                "This poem should be found",
                "Haiku"
        );

        Poem created = poemDAO.create(poem);

        Poem found = poemDAO.getPoemById(created.getId());

        assertThat(found, is(notNullValue()));
        assertThat(found.getId(), is(created.getId()));
        assertThat(found.getTitle(), is("Find me"));
    }

    @Test
    void getPoems() {

        Poem poem1 = new Poem(
                "Poem one",
                "First test poem",
                "Haiku"
        );

        Poem poem2 = new Poem(
                "Poem two",
                "Second test poem",
                "Haiku"
        );

        poemDAO.create(poem1);
        poemDAO.create(poem2);

        var poems = poemDAO.getPoems();

        assertThat(poems.size(), is(2));
    }

    @Test
    void updatePoem() {

        Poem original = new Poem(
                "Old title",
                "Old poem text",
                "Haiku"
        );

        Poem created = poemDAO.create(original);

        Poem updatedData = new Poem(
                "New title",
                "New poem text",
                "Free verse"
        );

        Poem updated = poemDAO.update(
                created.getId(),
                updatedData
        );

        assertThat(updated, is(notNullValue()));
        assertThat(updated.getId(), is(created.getId()));
        assertThat(updated.getTitle(), is("New title"));
        assertThat(updated.getPoem(), is("New poem text"));
        assertThat(updated.getStyle(), is("Free verse"));
    }

    @Test
    void deletePoem() {

        Poem poem = new Poem(
                "Delete me",
                "This poem should be deleted",
                "Haiku"
        );

        Poem created = poemDAO.create(poem);

        boolean deleted = poemDAO.delete(created.getId());

        Poem found = poemDAO.getPoemById(created.getId());

        assertThat(deleted, is(true));
        assertThat(found, is(nullValue()));
    }

    @Test
    void getPoemByIdReturnsNullWhenNotFound() {

        Poem found = poemDAO.getPoemById(9999);

        assertThat(found, is(nullValue()));
    }

    @Test
    void deleteReturnsFalseWhenPoemDoesNotExist() {

        boolean deleted = poemDAO.delete(9999);

        assertThat(deleted, is(false));
    }
}