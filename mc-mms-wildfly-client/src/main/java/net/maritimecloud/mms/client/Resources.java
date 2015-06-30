package net.maritimecloud.mms.client;

import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Produces the DB entity manager, making it available for CDI injection.
 */
public class Resources {

    @PersistenceContext(name = "mms-wildfly-client-ds")
    EntityManager entityManager;

    @Produces
    public EntityManager getEntityManager() {
        return entityManager;
    }

}
