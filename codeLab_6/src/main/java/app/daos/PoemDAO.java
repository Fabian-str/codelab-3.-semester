package app.daos;

import app.entities.Poem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.List;

public class PoemDAO {

    private final EntityManagerFactory emf;

    public PoemDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public List<Poem> getPoems() {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Poem> query =
                    em.createQuery("SELECT p FROM Poem p", Poem.class);

            return query.getResultList();
        }
    }

    public Poem create(Poem poem) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            em.persist(poem);

            em.getTransaction().commit();

            return poem;
        }
    }

    public List<Poem> createFromList(List<Poem> poems) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            List<Poem> createdPoems = new ArrayList<>();

            for (Poem poem : poems) {
                em.persist(poem);
                createdPoems.add(poem);
            }

            em.getTransaction().commit();

            return createdPoems;
        }
    }

    public Poem getPoemById(int id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(Poem.class, id);
        }
    }

    public boolean delete(int id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            Poem poem = em.find(Poem.class, id);

            if (poem != null) {
                em.remove(poem);
                em.getTransaction().commit();
                return true;
            }

            em.getTransaction().rollback();
            return false;
        }
    }

    public Poem update(int id, Poem updatedPoem) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            Poem poem = em.find(Poem.class, id);

            if (poem != null) {
                poem.setTitle(updatedPoem.getTitle());
                poem.setPoem(updatedPoem.getPoem());
                poem.setStyle(updatedPoem.getStyle());

                em.getTransaction().commit();

                return poem;
            }

            em.getTransaction().rollback();
            return null;
        }
    }
}