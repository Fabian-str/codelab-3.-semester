package app.config;

import app.entities.Course;
import app.entities.Person;
import app.entities.Student;
import org.hibernate.cfg.Configuration;

final class EntityRegistry {

    private EntityRegistry() {}

    static void registerEntities(Configuration configuration) {
        configuration.addAnnotatedClass(Person.class);
        configuration.addAnnotatedClass(Student.class);
        configuration.addAnnotatedClass(Course.class);
    }
}