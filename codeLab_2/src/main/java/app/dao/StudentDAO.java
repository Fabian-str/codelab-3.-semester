package app.dao;

import app.entities.Student;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class StudentDAO implements IDAO<Student> {

    private final EntityManagerFactory emf;

    public StudentDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Student create(Student student) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            em.persist(student);

            em.getTransaction().commit();

            return student;
        }
    }

    @Override
    public List<Student> getAll() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                    "SELECT s FROM Student s",
                    Student.class
            ).getResultList();
        }
    }

    @Override
    public Student getById(int id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(Student.class, id);
        }
    }

    @Override
    public Student update(Student student) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            Student updated = em.merge(student);

            em.getTransaction().commit();

            return updated;
        }
    }

    @Override
    public boolean delete(int id) {
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
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT s FROM Student s WHERE s.course.id = :courseId",
                            Student.class
                    )
                    .setParameter("courseId", courseId)
                    .getResultList();
        }
    }

    public List<Student> findByTeacherId(int teacherId) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT s FROM Student s WHERE s.course.teacher.id = :teacherId",
                            Student.class
                    )
                    .setParameter("teacherId", teacherId)
                    .getResultList();
        }
    }
}