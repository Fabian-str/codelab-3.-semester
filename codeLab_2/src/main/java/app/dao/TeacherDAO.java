package app.dao;

import app.entities.Semester;
import app.entities.Teacher;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class TeacherDAO implements IDAO<Teacher> {

    private final EntityManagerFactory emf;

    public TeacherDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Teacher create(Teacher teacher) {

        try (EntityManager em = emf.createEntityManager()) {

            em.getTransaction().begin();

            em.persist(teacher);

            em.getTransaction().commit();

            return teacher;
        }
    }

    @Override
    public List<Teacher> getAll() {

        try (EntityManager em = emf.createEntityManager()) {

            return em.createQuery(
                    "SELECT t FROM Teacher t",
                    Teacher.class
            ).getResultList();
        }
    }

    @Override
    public Teacher getById(int id) {

        try (EntityManager em = emf.createEntityManager()) {

            return em.find(Teacher.class, id);
        }
    }

    @Override
    public Teacher update(Teacher teacher) {

        try (EntityManager em = emf.createEntityManager()) {

            em.getTransaction().begin();

            Teacher updatedTeacher =
                    em.merge(teacher);

            em.getTransaction().commit();

            return updatedTeacher;
        }
    }

    @Override
    public boolean delete(int id) {

        try (EntityManager em = emf.createEntityManager()) {

            em.getTransaction().begin();

            Teacher teacher =
                    em.find(Teacher.class, id);

            if (teacher == null) {
                em.getTransaction().rollback();
                return false;
            }

            em.remove(teacher);

            em.getTransaction().commit();

            return true;
        }
    }

    public List<Teacher> findBySemester(Semester semester) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            """
                            SELECT DISTINCT c.teacher
                            FROM Course c
                            WHERE c.semester = :semester
                            """,
                            Teacher.class
                    )
                    .setParameter("semester", semester)
                    .getResultList();
        }
    }
}