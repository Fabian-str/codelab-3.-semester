package app.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String phone;
    private String email;
    private String address;

    @Enumerated(EnumType.STRING)
    private StudentStatus status;

    private LocalDate dateOfBirth;
    private LocalDate dateOfEnrollment;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Integer> courseIds = new HashSet<>();

    public Student(
            String name,
            String phone,
            String email,
            String address,
            StudentStatus status,
            LocalDate dateOfBirth,
            LocalDate dateOfEnrollment
    ) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.status = status;
        this.dateOfBirth = dateOfBirth;
        this.dateOfEnrollment = dateOfEnrollment;
    }
}