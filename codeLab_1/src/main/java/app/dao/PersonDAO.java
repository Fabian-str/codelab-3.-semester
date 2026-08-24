package app.dao;

import app.entities.Person;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

public class PersonDAO {

    private final EntityManagerFactory emf;

    public PersonDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public Person createPerson(Person person) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(person);
            em.getTransaction().commit();

            return person;
        }
    }
}