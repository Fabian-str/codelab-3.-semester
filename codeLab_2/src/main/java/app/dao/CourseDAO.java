package app.dao;

import app.entities.Course;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class CourseDAO implements IDAO<Course> {

    private final EntityManagerFactory emf;

    public CourseDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Course create(Course course) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(course);
            em.getTransaction().commit();

            return course;
        }
    }

    @Override
    public List<Course> getAll() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                    "SELECT c FROM Course c",
                    Course.class
            ).getResultList();
        }
    }

    @Override
    public Course getById(int id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(Course.class, id);
        }
    }

    @Override
    public Course update(Course course) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            Course updated = em.merge(course);

            em.getTransaction().commit();

            return updated;
        }
    }

    @Override
    public boolean delete(int id) {
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

    public Course findCourseByStudentId(int studentId) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT s.course FROM Student s WHERE s.id = :studentId",
                            Course.class
                    )
                    .setParameter("studentId", studentId)
                    .getSingleResult();
        }
    }

    public List<Course> findByTeacherId(int teacherId) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT c FROM Course c WHERE c.teacher.id = :teacherId",
                            Course.class
                    )
                    .setParameter("teacherId", teacherId)
                    .getResultList();
        }
    }
}