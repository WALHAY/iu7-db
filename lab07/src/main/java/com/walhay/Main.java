package com.walhay;

import com.walhay.dto.*;
import com.walhay.models.Game;
import com.walhay.models.User;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public class Main {
    private static void printCollection(Collection<?> collection) {
        if(collection == null || collection.isEmpty()) {
            System.out.println("\nПусто");
            return;
        }
        collection.stream().map(Object::toString).forEach(System.out::println);
    }

    public static void main(String[] args) {
        GameService gameService = new GameService();

        // получение суммарной выручки сервиса
        BigDecimal revenue = gameService.getTotalServiceRevenue();
        System.out.println("\nОбщая выручка сервиса: " + revenue);

        // получение данных о покупках игр
        List<GameBuyData> gamesBuyData = gameService.getGamesBuyData();
        System.out.println("\nДанные о покупках игр:");
        printCollection(gamesBuyData);

        // поиск пользователей с количеством игр > n
        List<User> users = gameService.getUsersWithGames(3);
        System.out.println("\nПользователи с более чем 3 играми:");
        printCollection(users);

        // топ 5 издателей по продажам
        List<PublisherInfo> publishers = gameService.getTopPricePublishers(5);
        System.out.println("\nТоп 5 издателей:");
        printCollection(publishers);

        // топ 3 жанра по количеству игр
        List<GenreStats> genres = gameService.getTopGenresByGames(3);
        System.out.println("\nТоп 3 жанра:");
        printCollection(genres);

        // изменение информации о скидке для игры с id = 5
        SaleInfo newSaleInfo = new SaleInfo();
        newSaleInfo.setValue(25.0);
        newSaleInfo.setEnd(Date.valueOf(LocalDate.now().plusDays(7).toString()));
        newSaleInfo.setStart(null);
        gameService.changeGameSaleInfo(5, newSaleInfo);

        // установка даты начала скидки для игры с id 5
        gameService.addGameSaleStartDate(5, Date.valueOf(LocalDate.now().plusDays(3).toString()));

        SaleInfo saleInfo = gameService.getGameSaleInfo(5);
        System.out.println("\nИнформация о скидке для игры id = 5: " + saleInfo);

        // получение игр дороже 99
        List<Game> expensiveGames = gameService.getGamesByMinPrice(new BigDecimal("99.00"));
        System.out.println("\nИгры дороже 99.00$: ");
        printCollection(expensiveGames);

        // получение 10 последних изданных игр
        List<GamePublished> lastPublishedGames = gameService.getLastPublishedGames(10);
        System.out.println("\n10 последних изданных игр:");
        printCollection(lastPublishedGames);

        // добавление новой игры
        gameService.addGame("\nNew Game", "Description of new game", new BigDecimal("30.00"));

        // обновление цены игры id = 1
        int updatedRows = gameService.updateGamePriceById(1L, new BigDecimal("50.00"));
        System.out.println("\nОбновлено записей: " + updatedRows);

        // удаление игры id = 2
        gameService.deleteGameById(2L);
        System.out.println("\nУдалена игра с id = 2");

        // вызов хранимой процедуры
        int count = gameService.callGetPublishersCount();
        System.out.println("\nКоличество издателей: " + count);
        HibernateUtil.shutdown();
    }
}
