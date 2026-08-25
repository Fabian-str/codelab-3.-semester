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

public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    @Enumerated(EnumType.STRING)
    private Semester semester;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    @ToString.Exclude
    private Teacher teacher;

    @OneToMany(mappedBy = "course")
    @ToString.Exclude
    private List<Student> students = new ArrayList<>();

    public Course(String name, Semester semester) {
        this.name = name;
        this.semester = semester;
    }

    public void addStudent(Student student) {
        students.add(student);
        student.setCourse(this);
    }
}