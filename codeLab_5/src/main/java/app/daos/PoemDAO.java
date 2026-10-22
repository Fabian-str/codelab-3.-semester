package app.daos;

import app.dtos.PoemDTO;
import app.entities.Poem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class PoemDAO {

    private final EntityManagerFactory emf;

    public PoemDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public List<PoemDTO> readAll() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT p FROM Poem p ORDER BY p.id", Poem.class)
                .getResultList()
                .stream()
                .map(PoemDTO::new)
                .toList();
        }
    }

    public PoemDTO create(PoemDTO dto) {
        try (EntityManager em = emf.createEntityManager()) {
            var transaction = em.getTransaction();

            try {
                transaction.begin();

                Poem poem = new Poem(dto.getTitle(), dto.getAuthor(), dto.getContent());

                em.persist(poem);
                transaction.commit();

                return new PoemDTO(poem);
            } catch (RuntimeException e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw e;
            }
        }
    }

    public PoemDTO read(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            Poem poem = em.find(Poem.class, id);

            return poem == null ? null : new PoemDTO(poem);
        }
    }

    public PoemDTO update(Integer id, PoemDTO dto) {
        try (EntityManager em = emf.createEntityManager()) {
            var transaction = em.getTransaction();

            try {
                transaction.begin();

                Poem poem = em.find(Poem.class, id);

                if (poem == null) {
                    transaction.rollback();
                    return null;
                }

                poem.setTitle(dto.getTitle());
                poem.setAuthor(dto.getAuthor());
                poem.setContent(dto.getContent());

                transaction.commit();

                return new PoemDTO(poem);
            } catch (RuntimeException e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw e;
            }
        }
    }

    public boolean delete(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            var transaction = em.getTransaction();

            try {
                transaction.begin();

                Poem poem = em.find(Poem.class, id);

                if (poem == null) {
                    transaction.rollback();
                    return false;
                }

                em.remove(poem);
                transaction.commit();

                return true;
            } catch (RuntimeException e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw e;
            }
        }
    }
}