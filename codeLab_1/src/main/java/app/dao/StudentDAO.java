package app.dao;

import app.entities.Student;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;
import java.util.Optional;

public class StudentDAO {

    private final EntityManagerFactory emf;

    public StudentDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public Student create(Student student) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(student);
            em.getTransaction().commit();

            return student;
        }
    }

    public Optional<Student> findById(int id) {
        try (EntityManager em = emf.createEntityManager()) {
            return Optional.ofNullable(em.find(Student.class, id));
        }
    }

    public List<Student> findAll() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT s FROM Student s", Student.class).getResultList();
        }
    }

    public Student update(Student student) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            Student updated = em.merge(student);

            em.getTransaction().commit();

            return updated;
        }
    }

    public boolean deleteById(int id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            Student student = em.find(Student.class, id);

            if (student == null) {
                em.getTransaction().rollback();
                return false;
            }

            em.remove(student);
            em.getTransaction().commit();

            return true;
        }
    }

    public List<Student> findByCourseId(int courseId) {
        return findAll()
                .stream()
                .filter(student -> student.getCourseIds()
                .contains(courseId))
                .toList();
    }
}