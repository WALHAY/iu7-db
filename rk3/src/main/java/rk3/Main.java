package rk3;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.stream.Stream;

public class Main {
    private static final String URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";
    private static final SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();

    public static void printResult(Collection<?> collection)
    {
        Stream.of(collection).map(Object::toString).forEach(System.out::println);
    }

    public static void main(String[] args) throws SQLException {
        /*
        JDBC - SQL
         */
        Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
        // 1
        printResult(JDBC.getRegionsWithFiveYearDrivers(connection));
        // 2
        printResult(JDBC.findDriversWithMoreThanFiveTrips(connection));

        /*
        HIBERNATE - ORM
         */
        var session = sessionFactory.openSession();
        var tr = session.beginTransaction();

        // 1
        printResult(ORM.getRegionsWithFiveYearDrivers(session));
        // 2
        printResult(ORM.findDriversWithMoreThanFiveTrips(session));

        tr.commit();

        session.close();
        sessionFactory.close();
    }
}
