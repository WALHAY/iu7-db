package com.walhay;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class LibraryManager {
    private final Connection connection;

    LibraryManager(Connection connection) throws SQLException {
        this.connection = connection;
    }

    public String getNameByLogin(String login) throws SQLException {
        String query = """
                select nickname
                from library.users
                where login = ?
                """;

        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, login);

        var result = statement.executeQuery();
        if(result.next())
            return result.getString("nickname");
        return "Undefined";
    }

    public Set<String> getUserGames(String login) throws SQLException {
        String query = """
                select g.name
                from library.users u
                join library.users_games ug on u.login = ug.user_login
                join library.games g on g.id = ug.game_id
                where u.login = ?
                order by g.id
                """;

        Set<String> result = new HashSet<>();
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, login);

        var queryResult = statement.executeQuery();
        if(queryResult.next())
            result.add(queryResult.getString("name"));
        return result;
    }

    // OTB

    public record PublisherData(String publisherName, double avgPrice) {}

    public Set<PublisherData> getPublisherData() throws SQLException {
        String query = """
                with publisher_stats as (
                    select
                        p.name as publisher_name,
                        g.price::numeric as price
                    from library.publishers p
                    join library.publishers_games_genres pgg on p.name = pgg.publisher_name
                    join library.games g on pgg.game_id = g.id
                )
                select distinct
                    publisher_name,
                    avg(price) over (partition by publisher_name) as avg_price
                from publisher_stats
                order by avg_price desc;
                """;

        Set<PublisherData> result = new HashSet<>();
        Statement statement = connection.createStatement();

        var queryResult = statement.executeQuery(query);
        while(queryResult.next())
        {
            String name = queryResult.getString("publisher_name");
            double price = queryResult.getDouble("avg_price");

            result.add(new PublisherData(name, price));
        }
        return result;
    }

    public Set<String> getTableNames() throws SQLException {
        String query = """
                select table_name
                from information_schema.tables
                where table_schema = 'library'
                order by table_name;
                """;

        PreparedStatement statement = connection.prepareStatement(query);
        var queryResult = statement.executeQuery();

        Set<String> tables = new HashSet<>();
        while(queryResult.next()) {
            tables.add(queryResult.getString("table_name"));
        }
        return tables;
    }

    public int callGetPriceById(int id) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("select get_price_by_id(?) as price");
        statement.setInt(1, id);

        var queryResult = statement.executeQuery();
        if(queryResult.next())
            return queryResult.getInt("price");
        return -1;
    }

    // table func
    public record GenreDescription(String genre, String description){};

    public Set<GenreDescription> callGetGenreByRestriction(int restriction) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("select * from get_genre_by_restriction(?)");
        statement.setInt(1, restriction);

        Set<GenreDescription> result = new HashSet<>();

        var queryResult = statement.executeQuery();
        while(queryResult.next()) {
            String genre = queryResult.getString("name");
            String description = queryResult.getString("description");

            GenreDescription gd = new GenreDescription(genre, description);

            result.add(gd);
        }

        return result;
    }

    public void adjustPrice(int percentage) throws SQLException {
        var statement = connection.prepareStatement("call adjust_price(?)");
        statement.setInt(1, percentage);
        statement.execute();
    }

    public String getVersion() throws SQLException {
        var statement = connection.prepareStatement("select version()");

        var queryResult = statement.executeQuery();
        if(queryResult.next())
            return queryResult.getString("version");
        return "Undefined";
    }

    public void createTable() throws SQLException {
        String query = """
                drop table if exists library.comments;
                create table if not exists library.comments(
                id SERIAL PRIMARY KEY,
                user_login VARCHAR,
                text TEXT,
                CONSTRAINT fk_comment FOREIGN KEY(user_login) references library.users(login)
                );
                """;

        var statement = connection.prepareStatement(query);
        statement.execute();
    }

    public void insertComment(String login, String comment) throws SQLException {
        String query = """
                insert into library.comments(user_login, text)
                values (?, ?)
                """;

        var statement = connection.prepareStatement(query);

        statement.setString(1, login);
        statement.setString(2, comment);

        statement.execute();
    }

    public void deleteDatabase(String database) throws SQLException {
        String query = String.format("drop database %s with (force)", database);

        var statement = connection.prepareStatement(query);

        statement.execute();
    }

    public void createDatabase(String database) throws SQLException {
        String query = String.format("create database %s;", database);

        var statement = connection.prepareStatement(query);

        statement.execute();
    }
}
