package app.dao;

import app.entities.Course;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;
import java.util.Optional;

public class CourseDAO {

    private final EntityManagerFactory emf;

    public CourseDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public Course create(Course course) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(course);
            em.getTransaction().commit();

            return course;
        }
    }

    public Optional<Course> findById(int id) {
        try (EntityManager em = emf.createEntityManager()) {
            return Optional.ofNullable(em.find(Course.class, id));
        }
    }

    public List<Course> findAll() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                    "SELECT c FROM Course c", Course.class).getResultList();
        }
    }

    public Course update(Course course) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            Course updated = em.merge(course);

            em.getTransaction().commit();

            return updated;
        }
    }

    public boolean deleteById(int id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            Course course = em.find(Course.class, id);

            if (course == null) {
                em.getTransaction().rollback();
                return false;
            }

            em.remove(course);
            em.getTransaction().commit();

            return true;
        }
    }
}