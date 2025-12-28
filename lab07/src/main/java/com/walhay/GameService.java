package com.walhay;

import com.walhay.dto.*;
import com.walhay.models.*;
import jakarta.persistence.criteria.*;
import org.hibernate.Session;
import org.hibernate.query.MutationQuery;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

public class GameService {
    // N1
    // суммарное количество выручки сервиса
    public BigDecimal getTotalServiceRevenue() {
        try(Session session = HibernateUtil.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<BigDecimal> cq = cb.createQuery(BigDecimal.class);

            Root<Game> gameRoot = cq.from(Game.class);
            gameRoot.join("users");

            Expression<BigDecimal> totalRevenue = cb.sum(gameRoot.get("price").as(BigDecimal.class));
            cq.select(totalRevenue);

            BigDecimal result = session.createQuery(cq).getSingleResult();
            return result != null ? result : BigDecimal.ZERO;        }
    }

    // суммарное количество продаж и выручку с игры
    public List<GameBuyData> getGamesBuyData() {
        try(Session session = HibernateUtil.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();

            CriteriaQuery<GameBuyData> cq = cb.createQuery(GameBuyData.class);

            Root<Game> gameRoot = cq.from(Game.class);

            Join<User, Game> usersJoin = gameRoot.join("users");
            Expression<Long> countUsers = cb.count(usersJoin.get("login"));
            Expression<Long> totalMoney = cb.sum(gameRoot.get("price"));

            cq.select(cb.construct(
                    GameBuyData.class,
                    gameRoot.get("id"),
                    gameRoot.get("name"),
                    countUsers.alias("players"),
                    totalMoney
            ))
                    .groupBy(gameRoot)
                    .orderBy(cb.desc(countUsers));

            return session.createQuery(cq).getResultList();
        }
    }

    // найти пользователей с количеством игр >= n
    public List<User> getUsersWithGames(int games) {
        try (Session session = HibernateUtil.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();

            CriteriaQuery<User> cq = cb.createQuery(User.class);

            Root<User> pggRoot = cq.from(User.class);

            Join<User, Game> gamesJoin = pggRoot.join("games");

            cq.groupBy(pggRoot.get("login"));

            Expression<Long> countDistinctGames = cb.countDistinct(gamesJoin);
            cq.having(cb.ge(countDistinctGames, games));

            return session.createQuery(cq).getResultList();
        }
    }

    // найти топ n издателей по продажам
    public List<PublisherInfo> getTopPricePublishers(int limit) {
        try(Session session = HibernateUtil.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<PublisherInfo> cq = cb.createQuery(PublisherInfo.class);

            Root<PublisherGameGenre> pggRoot = cq.from(PublisherGameGenre.class);
            Join<PublisherGameGenre, Publisher> publisherJoin = pggRoot.join("publisher");
            Join<PublisherGameGenre, Game> gameJoin = pggRoot.join("game");
            Join<Game, User> userJoin = gameJoin.join("users");

            Expression<Long> distinctGames = cb.countDistinct(gameJoin.get("id"));
            Expression<Long> totalUsers = cb.count(userJoin.get("login"));

            Expression<BigDecimal> totalRevenue = cb.sum(gameJoin.get("price"));

            cq.select(cb.construct(
                    PublisherInfo.class,
                    publisherJoin.get("name"),
                    distinctGames,
                    totalUsers,
                    totalRevenue
            ));

            cq.groupBy(publisherJoin.get("name"));
            cq.orderBy(cb.desc(totalRevenue));

            return session.createQuery(cq).setMaxResults(limit).getResultList();
        }
    }

    // найти топ n жанров по количесту игр
    public List<GenreStats> getTopGenresByGames(int limit) {
        try (Session session = HibernateUtil.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<GenreStats> cq = cb.createQuery(GenreStats.class);

            Root<PublisherGameGenre> pggRoot = cq.from(PublisherGameGenre.class);
            Join<PublisherGameGenre, Genre> genreJoin = pggRoot.join("genre");
            Join<PublisherGameGenre, Game> gameJoin = pggRoot.join("game");

            Expression<Long> gameCount = cb.countDistinct(gameJoin.get("id"));

            cq.select(cb.construct(
                    GenreStats.class,
                    genreJoin.get("name"),
                    gameCount
            ));

            cq.groupBy(genreJoin.get("name"));
            cq.orderBy(cb.desc(gameCount));

            return session.createQuery(cq).setMaxResults(limit).getResultList();
        }
    }

    // N2
    public SaleInfo getGameSaleInfo(int id) {
        try (Session session = HibernateUtil.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();

            CriteriaQuery<SaleInfo> cq = cb.createQuery(SaleInfo.class);

            Root<Game> games = cq.from(Game.class);

            cq.select(games.get("sale"))
                    .where(cb.and(
                            cb.equal(games.get("id"), id),
                            cb.isNotNull(games.get("sale"))
                            )
                    );

            return session.createQuery(cq).getSingleResult();
        }
    }

    public void changeGameSaleInfo(int id, SaleInfo saleInfo) {
        try (Session session = HibernateUtil.openSession()) {
            var transaction = session.beginTransaction();
            CriteriaBuilder cb = session.getCriteriaBuilder();

            CriteriaUpdate<Game> cq = cb.createCriteriaUpdate(Game.class);

            Root<Game> games = cq.from(Game.class);

            cq.set(games.get("sale").get("value"), saleInfo.getValue())
                    .set(games.get("sale").get("end"), saleInfo.getEnd())
                    .where(cb.equal(games.get("id"), id));

            session.createMutationQuery(cq).executeUpdate();

            transaction.commit();
        }
    }

    public void addGameSaleStartDate(int id, Date start) {
        try (Session session = HibernateUtil.openSession()) {
            var transaction = session.beginTransaction();
            CriteriaBuilder cb = session.getCriteriaBuilder();

            CriteriaUpdate<Game> cq = cb.createCriteriaUpdate(Game.class);

            Root<Game> games = cq.from(Game.class);

            cq.set(games.get("sale").get("start"), start)
                    .where(cb.equal(games.get("id"), id));

            session.createMutationQuery(cq).executeUpdate();

            transaction.commit();
        }
    }

    // N3
    // однотабличный: все игры дороже цены
    public List<Game> getGamesByMinPrice(BigDecimal minPrice) {
        try (Session session = HibernateUtil.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Game> cq = cb.createQuery(Game.class);
            Root<Game> gameRoot = cq.from(Game.class);

            cq.select(gameRoot)
                    .where(cb.greaterThan(cb.toBigDecimal(gameRoot.get("price")), minPrice))
                    .orderBy(cb.asc(gameRoot.get("name")));

            return session.createQuery(cq).getResultList();
        }
    }

    // многотабличный: топ n последних изданных игр
    public List<GamePublished> getLastPublishedGames(int games) {
        try (Session session = HibernateUtil.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<GamePublished> cq = cb.createQuery(GamePublished.class);

            Root<PublisherGameGenre> pggRoot = cq.from(PublisherGameGenre.class);
            Join<PublisherGameGenre, Game> gameJoin = pggRoot.join("game");

            cq.multiselect(
                    gameJoin.get("id"),
                    gameJoin.get("name"),
                    pggRoot.get("publishDate")
            ).orderBy(cb.desc(pggRoot.get("publishDate")));

            return session.createQuery(cq).setMaxResults(games).getResultList();
        }
    }

    // добавить игру
    public void addGame(String name, String description, BigDecimal price) {
        try (Session session = HibernateUtil.openSession()) {
            session.beginTransaction();

            Game newGame = new Game();
            newGame.setName(name);
            newGame.setDescription(description);
            newGame.setPrice(price);
            newGame.setSale(null);

            session.persist(newGame);
            session.getTransaction().commit();

        }
    }

    // обновить цену игры по ид
    public int updateGamePriceById(Long gameId, BigDecimal newPrice) {
        try (Session session = HibernateUtil.openSession()) {
            session.beginTransaction();

            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaUpdate<Game> update = cb.createCriteriaUpdate(Game.class);
            Root<Game> gameRoot = update.from(Game.class);

            update.set("price", newPrice)
                    .where(cb.equal(gameRoot.get("id"), gameId));

            MutationQuery query = session.createMutationQuery(update);
            int result = query.executeUpdate();

            session.getTransaction().commit();
            return result;
        }
    }

    // удалить игру по ид
    public void deleteGameById(Long gameId) {
        try (Session session = HibernateUtil.openSession()) {
            session.beginTransaction();

            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaDelete<Game> delete = cb.createCriteriaDelete(Game.class);
            Root<Game> gameRoot = delete.from(Game.class);

            delete.where(cb.equal(gameRoot.get("id"), gameId));

            MutationQuery query = session.createMutationQuery(delete);
            query.executeUpdate();

            session.getTransaction().commit();
        }
    }

    // вызов процедуры количества издателей
    public Integer callGetPublishersCount() {
        try (Session session = HibernateUtil.openSession()) {
            return session.createNativeQuery(
                    "call library.get_publishers_count(:publishers)", Integer.class
            ).setParameter("publishers", 0).getSingleResult();
        }
    }
}
