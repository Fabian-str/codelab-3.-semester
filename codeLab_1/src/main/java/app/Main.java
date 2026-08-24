package app;

import app.config.HibernateConfig;
import app.dao.CourseDAO;
import app.dao.PersonDAO;
import app.dao.StudentDAO;
import app.entities.Course;
import app.entities.Person;
import app.entities.Student;
import app.entities.StudentStatus;
import jakarta.persistence.EntityManagerFactory;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

        StudentDAO studentDAO = new StudentDAO(emf);

        CourseDAO courseDAO = new CourseDAO(emf);

        Course java = courseDAO.create(new Course(
            "Java", "Teacher A",
            "3rd semester", "Room 1",
            "08:30"
            )
        );

        Course databases = courseDAO.create(new Course(
            "Databases", "Teacher B",
            "3rd semester", "Room 2",
            "12:00"
                )
        );

        Student student = new Student(
            "Alice",
            "12345678",
            "alice@test.dk",
            "Testvej 1",
            StudentStatus.ACTIVE,
            LocalDate.of(2000, 1, 1),
            LocalDate.now()
        );

        student.getCourseIds().add(java.getId());
        student.getCourseIds().add(databases.getId());

        studentDAO.create(student);

        System.out.println(
                studentDAO.findAll()
        );

        System.out.println(
                courseDAO.findAll()
        );

        System.out.println(
                studentDAO.findByCourseId(
                        java.getId()
                )
        );
    }
}