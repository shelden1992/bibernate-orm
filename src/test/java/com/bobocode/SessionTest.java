package com.bobocode;

import com.bobocode.bibernateorm.Session;
import com.bobocode.entity.Person;
import com.bobocode.util.FileReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.postgresql.ds.PGSimpleDataSource;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class SessionTest {
    Session session;

    @BeforeEach
    void init() {
        doInHibernateSession(em -> {
            var create = FileReader.readWholeFileFromResources("sql_scripts/create.sql");
            var populate = FileReader.readWholeFileFromResources("sql_scripts/populate.sql");
            em.createNativeQuery(create).executeUpdate();
            em.createNativeQuery(populate).executeUpdate();
        });

        session = initNewSession();
    }

    private void doInHibernateSession(Consumer<EntityManager> entityManagerConsumer) {
        var managerFactory = Persistence.createEntityManagerFactory("default");
        var entityManager = managerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManagerConsumer.accept(entityManager);
        entityManager.getTransaction().commit();
    }

    private Session initNewSession() {
        var pgSimpleDataSource = new PGSimpleDataSource();
        pgSimpleDataSource.setURL("jdbc:postgresql://localhost:5432/postgres");
        pgSimpleDataSource.setUser("macuser");
        pgSimpleDataSource.setPassword("postgres");
        return new Session(pgSimpleDataSource);
    }

    @org.junit.jupiter.api.Test
    void shouldSelectPersonFromDbWhenValidId() {
        var person = session.find(Person.class, 1L);
        assertThat(person, is(notNullValue()));
        assertThat(person.getId(), is(1L));
    }

    @org.junit.jupiter.api.Test
    void shouldReturnNullWhenEntityNotPresentOnDb() {
        Assertions.assertNull(session.find(Person.class, 2));
    }

    @org.junit.jupiter.api.Test
    void shouldUpdatePersonIntoDbWhenCloseSession() {
        var person = session.find(Person.class, 1L);
        person.setFirstName("Change First Name");
        person.setLastName("Change Second Name");

        session.close();

        var session = initNewSession();

        var changePerson = session.find(Person.class, 1L);

        assertNotNull(changePerson);
        assertThat(changePerson.getId(), is(1L));
        assertThat(changePerson.getFirstName(), is(equalTo("Change First Name")));
        assertThat(changePerson.getLastName(), is(equalTo("Change Second Name")));

    }

    @org.junit.jupiter.api.Test
    void shouldSavePerson() {
        var person = new Person();
        person.setId(2L);
        person.setFirstName("Denys");
        person.setLastName("Shelupets");

        session.persist(person);
        session.flush();

        var findPerson = session.find(Person.class, 2L);

        assertNotNull(findPerson);
        assertEquals(findPerson.getId(), 2L);
    }

    @org.junit.jupiter.api.Test
    void shouldRemovePerson() {
        var findPerson = session.find(Person.class, 1L);

        assertNotNull(findPerson);
        assertEquals(findPerson.getId(), 1L);

        session.remove(findPerson);
        session.flush();

        var findAfterRemove = session.find(Person.class, 1L);
        Assertions.assertNull(findAfterRemove);
    }

    @org.junit.jupiter.api.Test
    void shouldNotDeleteWithoutFlush() {
        var findPerson = session.find(Person.class, 1L);

        assertNotNull(findPerson);
        assertEquals(findPerson.getId(), 1L);

        session.remove(findPerson);

        var findAfterRemove = session.find(Person.class, 1L);
        Assertions.assertNotNull(findAfterRemove);
    }

    @org.junit.jupiter.api.Test
    void shouldFirstlyPersistAndDeleteAfterFlush() {
        var person = new Person();
        person.setId(2L);
        person.setFirstName("Denys");
        person.setLastName("Shelupets");

        session.update(person);
        session.remove(person);
        session.flush();

        var findPerson = session.find(Person.class, 2L);

        assertNull(findPerson);
    }

}