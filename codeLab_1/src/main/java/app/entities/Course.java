package app.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String teacher;
    private String semester;
    private String classroom;
    private String timeOfCourse;

    public Course(
        String name,
        String teacher,
        String semester,
        String classroom,
        String timeOfCourse
    ) {
        this.name = name;
        this.teacher = teacher;
        this.semester = semester;
        this.classroom = classroom;
        this.timeOfCourse = timeOfCourse;
    }
}