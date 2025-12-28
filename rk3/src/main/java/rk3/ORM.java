package rk3;

import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import rk3.models.Driver;
import rk3.models.Route;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class ORM {

     public static List<String> getRegionsWithFiveYearDrivers(Session session) {
        var cb = session.getCriteriaBuilder();
        CriteriaQuery<String> cq = cb.createQuery(String.class);

        var root = cq.from(Driver.class);

        cq.select(root.get("region"))
                .where(cb.lessThanOrEqualTo(root.get("enterJob"), cb.literal(Date.valueOf(LocalDate.now().minusYears(5)))))
                .groupBy(root.get("region"));

        return session.createQuery(cq).getResultList();
    }

    public static List<Driver> findDriversWithMoreThanFiveTrips(Session session) {
        var cb = session.getCriteriaBuilder();
        CriteriaQuery<Driver> cq = cb.createQuery(Driver.class);

        Root<Driver> root = cq.from(Driver.class);
        Join<Driver, Route> routeJoin = root.join("driver_id");

        cq.select(root)
                .where(cb.equal(routeJoin.get("type"), 0L))
                .groupBy(root.get("driver_id"))
                .having(cb.gt(cb.count(routeJoin), 5L));

        return session.createQuery(cq).getResultList();
    }
}
