package com.walhay.lab09;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.postgresql.core.Tuple;
import redis.clients.jedis.Jedis;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class LibraryManager {

    private final Gson gson = new Gson();
    private final Connection connection;
    private final Jedis redis;
    private int id = 1000;

    public LibraryManager(Connection connection, Jedis redis) {
        this.connection = connection;
        this.redis = redis;
    }

    public List<String> getGamesByGenrePostgres(String genre) throws SQLException {
        String query = """
                select
                    g.name as name,
                    g.price as price,
                    pgg.genre_name as genre
                from library.games g
                join library.publishers_games_genres pgg
                    on g.id = pgg.game_id
                where pgg.genre_name = ?
                order by pgg.genre_name;
                """;

        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, genre);

        ResultSet queryResult = statement.executeQuery();

        List<String> result = new ArrayList<>();
        while(queryResult.next()) {
            result.add(queryResult.getString("name"));
        }
        return result;
    }

    public List<String> getGamesByGenreRedis(String genre) throws SQLException {
        String query = """
                select
                    g.name as name,
                    g.price as price,
                    pgg.genre_name as genre
                from library.games g
                join library.publishers_games_genres pgg
                    on g.id = pgg.game_id
                where pgg.genre_name = ?
                order by pgg.genre_name;
                """;

        String key = String.format("games_%s", genre);
        String cached = redis.get(key);
        if(cached != null) {
            return gson.fromJson(cached, new TypeToken<List<String>>(){}.getType());
        }

        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, genre);

        ResultSet queryResult = statement.executeQuery();

        List<String> result = new ArrayList<>();
        while(queryResult.next()) {
            result.add(queryResult.getString("name"));
        }

        redis.set(key, gson.toJson(result));
        return result;
    }

    public void selectQuery(PrintWriter writer) throws SQLException {
        String query = """
                select name
                from library.games;
                """;

        PreparedStatement statement = connection.prepareStatement(query);

        long start = System.nanoTime();
        ResultSet queryResult = statement.executeQuery();
        long end = System.nanoTime();

        writer.print(end - start);
        writer.print(',');

        List<String> result = new ArrayList<>();
        while(queryResult.next()) {
            result.add(queryResult.getString("name"));
        }

        var json = gson.toJson(result);
        String cached = redis.get("redis_select");
        if(cached == null)
            redis.set("redis_select", json);

        start = System.nanoTime();
        redis.get("redis_select");
        end = System.nanoTime();

        writer.print(end - start);
        writer.print('\n');
    }

    private String generateRandomString(int len) {
        Random random = new Random();

        StringBuilder builder = new StringBuilder();

        for(int i = 0; i < len; ++i) {
            builder.append((char) random.nextInt(97, 122));
        }

        return builder.toString();
    }

    public String insertQuery(PrintWriter writer) throws SQLException {
        String query = """
                insert into library.users
                values (?, ?, ?, now());
                """;

        PreparedStatement statement = connection.prepareStatement(query);

        String login = generateRandomString(8);
        statement.setString(1, login);
        statement.setString(2, generateRandomString(10));
        statement.setString(3, generateRandomString(5));

        long start = System.nanoTime();
        statement.execute();
        long end = System.nanoTime();

        writer.print(end - start);
        writer.print(',');

        PreparedStatement select = connection.prepareStatement("""
                select *
                from library.users
                where login = ?;
                """);

        select.setString(1, login);

        ResultSet result = select.executeQuery();
        if(result != null && result.next()) {
            Map<String, String> entry = new HashMap<>();
            entry.put("login", result.getString("login"));
            entry.put("nickname", result.getString("nickname"));
            entry.put("password", result.getString("password"));
            entry.put("registration_date", result.getString("registration_date"));

            var json = gson.toJson(entry);
            start = System.nanoTime();
            redis.set(String.format("user_%s", login), json);
            end = System.nanoTime();

            writer.print(end - start);
            writer.print('\n');
        }

        return login;
    }

    public void updateQuery(String login, PrintWriter writer) throws SQLException {
        String query = """
                update library.users
                set nickname = ?
                where login = ?;
                """;

        PreparedStatement statement = connection.prepareStatement(query);

        statement.setString(1, login);
        statement.setString(2, generateRandomString(12));

        long start = System.nanoTime();
        statement.execute();
        long end = System.nanoTime();

        writer.print(end - start);
        writer.print(',');

        PreparedStatement select = connection.prepareStatement("""
                select *
                from library.users
                where login = ?;
                """);

        select.setString(1, login);

        ResultSet result = select.executeQuery();
        if(result != null && result.next()) {
            Map<String, String> entry = new HashMap<>();
            entry.put("login", result.getString("login"));
            entry.put("nickname", result.getString("nickname"));
            entry.put("password", result.getString("password"));
            entry.put("registration_date", result.getString("registration_date"));

            var json = gson.toJson(entry);
            start = System.nanoTime();
            redis.set(String.format("user_%s", login), json);
            end = System.nanoTime();

            writer.print(end - start);
            writer.print('\n');
        }
    }

    public void deleteQuery(String login, PrintWriter writer) throws SQLException {
        String query = """
                delete from library.users
                where login = ?;
                """;

        PreparedStatement statement = connection.prepareStatement(query);

        statement.setString(1, login);

        long start = System.nanoTime();
        statement.execute();
        long end = System.nanoTime();

        writer.print(end - start);
        writer.print(',');

        start = System.nanoTime();
        redis.del(String.format("user_%s", login));
        end = System.nanoTime();

        writer.print(end - start);
        writer.print('\n');
    }
}
