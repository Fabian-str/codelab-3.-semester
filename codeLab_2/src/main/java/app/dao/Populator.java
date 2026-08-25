package app;

import app.dao.*;
import app.entities.*;
import jakarta.persistence.EntityManagerFactory;

public class Populator {

    public static void populate(EntityManagerFactory emf) {

        TeacherDAO teacherDAO =
                new TeacherDAO(emf);

        CourseDAO courseDAO =
                new CourseDAO(emf);

        StudentDAO studentDAO =
                new StudentDAO(emf);


        Teacher teacher =
                new Teacher("John Smith");

        teacherDAO.create(teacher);


        Course java =
                new Course(
                        "Java",
                        Semester.FALL
                );

        java.setTeacher(teacher);

        courseDAO.create(java);


        Student alice =
                new Student("Alice");

        alice.setCourse(java);

        studentDAO.create(alice);


        Student bob =
                new Student("Bob");

        bob.setCourse(java);

        studentDAO.create(bob);
    }
}