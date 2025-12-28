package rk3;

import rk3.models.Driver;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JDBC {

    public static List<String> getRegionsWithFiveYearDrivers(Connection connection) throws SQLException {
        String query = """
                select region
                from rk3.drivers
                where enter_job <= current_date - interval '5 years'
                group by region
                """;

        var statement = connection.prepareStatement(query);

        ResultSet set = statement.executeQuery();

        List<String> regions = new ArrayList<>();
        while(set.next()) {
            regions.add(set.getString("region"));
        }
        return regions;
    }

    public static List<Driver> findDriversWithMoreThanFiveTrips(Connection connection) throws SQLException {
        String query = """
                select d.* from rk3.drivers d
                join rk3.routes r on d.id = r.driver_id
                where r.type = 0
                group by d.id
                having count(*) > 5
                """;

        var statement = connection.prepareStatement(query);

        ResultSet set = statement.executeQuery();

        List<Driver> drivers = new ArrayList<>();
        while(set.next()) {
            Driver driver = new Driver();

            driver.setId(set.getLong("id"));
            driver.setEnterJob(set.getDate("enter_job"));
            driver.setBirth(set.getDate("birth"));
            driver.setRegion(set.getString("region"));

            drivers.add(driver);
        }
        return drivers;
    }
}
