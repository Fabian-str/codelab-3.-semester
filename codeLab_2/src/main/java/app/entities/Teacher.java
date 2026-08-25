package app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString

public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    @OneToMany(mappedBy = "teacher")
    @ToString.Exclude
    private List<Course> courses = new ArrayList<>();

    public Teacher(String name) {
        this.name = name;
    }

    public void addCourse(Course course) {
        courses.add(course);
        course.setTeacher(this);
    }
}