package app;

import app.config.HibernateConfig;
import app.dao.CourseDAO;
import app.dao.StudentDAO;
import app.dao.TeacherDAO;
import app.entities.Course;
import app.entities.Semester;
import app.entities.Student;
import app.entities.Teacher;
import jakarta.persistence.EntityManagerFactory;

public class Main {

    public static void main(String[] args) {

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

        TeacherDAO teacherDAO = new TeacherDAO(emf);

        CourseDAO courseDAO = new CourseDAO(emf);

        StudentDAO studentDAO = new StudentDAO(emf);


        Teacher teacher = new Teacher("John");

        teacherDAO.create(teacher);


        Course course = new Course("JPA", Semester.FALL);

        course.setTeacher(teacher);

        courseDAO.create(course);


        Student student = new Student("Alice");

        student.setCourse(course);

        studentDAO.create(student);


        System.out.println(teacher);
        System.out.println(course);
        System.out.println(student);

        emf.close();
    }
}