package vn.edu.clinic.server;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public final class JpaUtil {
    private static final EntityManagerFactory FACTORY = Persistence.createEntityManagerFactory("clinicPU", properties());

    private static java.util.Map<String, String> properties() {
        String url = System.getProperty("clinic.db.url");
        return url == null ? java.util.Map.of() : java.util.Map.of("jakarta.persistence.jdbc.url", url);
    }

    private JpaUtil() {
    }

    public static EntityManager em() {
        return FACTORY.createEntityManager();
    }

    public static void close() {
        FACTORY.close();
    }
}
