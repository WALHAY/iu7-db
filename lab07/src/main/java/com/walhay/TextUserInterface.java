package com.walhay;

import com.walhay.dto.SaleInfo;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Scanner;

public class TextUserInterface {
    private final GameService service;
    private boolean running = false;
    private static final Scanner scanner = new Scanner(System.in);

    public TextUserInterface(GameService service) {
        this.service = service;
    }

    public void run() {
        running = true;
        while(running) {
            printOptions(); // РАСКОММЕНТИРОВАТЬ!
            try {
                int option = nextOption(); // Сохраняем результат
                executeOption(option); // Вызываем обработку опции
            } catch (Exception e) {
                System.err.println("Во время запроса произошла ошибка");
                e.printStackTrace();
            }
        }
    }

    private void printOptions() {
        String options = """
                1. Суммарная выручка сервиса;
                2. Количество продаж и выручка игры;
                3. Найти пользователей по количеству игр;
                4. Топ издателей по выручке;
                5. Топ жанров по количеству игр;
                6. Информация о скидке игры;
                7. Изменить скидку игры;
                8. Добавить начало скидки игры;
                9. Найти все игры дороже цены;
                10. Найти последние изданные игры;
                11. Добавить игру;
                12. Обновить цену игры;
                13. Удалить игру;
                14. Статистика по играм;
                0. Выйти.
                """;

        System.out.println(options);
    }

    private int nextOption() {
        while(true) {
            System.out.println("\nВведите опцию [0, 14]:");

            try {
                String input = scanner.nextLine();
                int value = Integer.parseInt(input);
                if (value >= 0 && value <= 14) {
                    return value;
                } else {
                    System.out.println("Некорректная опция. Введите число от 0 до 14.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите целое число.");
            }
        }
    }

    private void printItems(Collection<?> collection) {
        if (collection.isEmpty()) {
            System.out.println("\t(пусто)");
        } else {
            for(var item : collection)
                System.out.printf("\t- %s\n", item.toString());
        }
    }

    private void executeOption(int option) throws SQLException {
        switch (option) {
            case 0 -> {
                running = false;
                System.out.println("Выход из программы.");
            }
            case 1 -> {
                BigDecimal revenue = service.getTotalServiceRevenue();
                System.out.println("Суммарная выручка сервиса: " + revenue);
            }
            case 2 -> {
                var games = service.getGamesBuyData();
                System.out.println("Количество продаж и выручка игры:");
                printItems(games);
            }
            case 3 -> {
                System.out.println("Введите нижнюю границу количества игр: ");
                try {
                    int cnt = Integer.parseInt(scanner.nextLine());
                    var users = service.getUsersWithGames(cnt);
                    System.out.println("Пользователи:");
                    printItems(users);
                } catch (NumberFormatException e) {
                    System.out.println("Ошибка: введите целое число.");
                }
            }
            case 4 -> {
                System.out.println("Введите количество издателей для топ по выручке: ");
                try {
                    int cnt = Integer.parseInt(scanner.nextLine());
                    var publishers = service.getTopPricePublishers(cnt);
                    System.out.println("Издатели:");
                    printItems(publishers);
                } catch (NumberFormatException e) {
                    System.out.println("Ошибка: введите целое число.");
                }
            }
            case 5 -> {
                System.out.println("Введите количество жанров для топа: ");
                try {
                    int cnt = Integer.parseInt(scanner.nextLine());
                    var genres = service.getTopGenresByGames(cnt);
                    System.out.println("Жанры:");
                    printItems(genres);
                } catch (NumberFormatException e) {
                    System.out.println("Ошибка: введите целое число.");
                }
            }
            case 6 -> {
                System.out.println("Введите ID игры: ");
                try {
                    int id = Integer.parseInt(scanner.nextLine());
                    SaleInfo saleInfo = service.getGameSaleInfo(id);
                    if (saleInfo == null) {
                        System.out.println("Скидка не найдена");
                    } else {
                        System.out.println("Информация о скидке: " + saleInfo);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Ошибка: введите целое число.");
                }
            }
            case 7 -> {
                try {
                    System.out.println("Введите ID игры: ");
                    int id = Integer.parseInt(scanner.nextLine());
                    System.out.println("Введите значение скидки (десятичное число): ");
                    Double value = Double.parseDouble(scanner.nextLine());
                    System.out.println("Введите дату окончания скидки (yyyy-MM-dd): ");
                    String end = scanner.nextLine();

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    java.util.Date date = sdf.parse(end);
                    Date endDate = new Date(date.getTime());

                    SaleInfo saleInfo = new SaleInfo(value, endDate, null);
                    service.changeGameSaleInfo(id, saleInfo);
                    System.out.println("Скидка изменена");
                } catch (NumberFormatException e) {
                    System.out.println("Ошибка формата числа.");
                } catch (ParseException e) {
                    System.out.println("Не удалось прочитать дату. Используйте формат yyyy-MM-dd");
                }
            }
            case 8 -> {
                try {
                    System.out.println("Введите ID игры: ");
                    int id = Integer.parseInt(scanner.nextLine());
                    System.out.println("Введите дату начала скидки (строка): ");
                    String start = scanner.nextLine();
                    service.addGameSaleStartDate(id, Date.valueOf(start));
                    System.out.println("Дата начала скидки добавлена");
                } catch (NumberFormatException e) {
                    System.out.println("Ошибка: введите целое число.");
                }
            }
            case 9 -> {
                try {
                    System.out.println("Введите минимальную цену: ");
                    BigDecimal minPrice = new BigDecimal(scanner.nextLine());
                    var games = service.getGamesByMinPrice(minPrice);
                    System.out.println("Игры дороже " + minPrice + ":");
                    printItems(games);
                } catch (NumberFormatException e) {
                    System.out.println("Ошибка формата числа.");
                }
            }
            case 10 -> {
                try {
                    System.out.println("Введите количество игр: ");
                    int cnt = Integer.parseInt(scanner.nextLine());
                    var games = service.getLastPublishedGames(cnt);
                    System.out.println("Последние изданные игры:");
                    printItems(games);
                } catch (NumberFormatException e) {
                    System.out.println("Ошибка: введите целое число.");
                }
            }
            case 11 -> {
                System.out.println("Введите название игры: ");
                String name = scanner.nextLine();
                System.out.println("Введите описание игры: ");
                String description = scanner.nextLine();
                try {
                    System.out.println("Введите цену игры: ");
                    BigDecimal price = new BigDecimal(scanner.nextLine());
                    service.addGame(name, description, price);
                    System.out.println("Игра добавлена");
                } catch (NumberFormatException e) {
                    System.out.println("Ошибка формата числа.");
                }
            }
            case 12 -> {
                try {
                    System.out.println("Введите ID игры: ");
                    long gameId = Long.parseLong(scanner.nextLine());
                    System.out.println("Введите новую цену: ");
                    BigDecimal newPrice = new BigDecimal(scanner.nextLine());
                    int result = service.updateGamePriceById(gameId, newPrice);
                    System.out.println("Обновлено записей: " + result);
                } catch (NumberFormatException e) {
                    System.out.println("Ошибка формата числа.");
                }
            }
            case 13 -> {
                try {
                    System.out.println("Введите ID игры: ");
                    long gameId = Long.parseLong(scanner.nextLine());
                    service.deleteGameById(gameId);
                } catch (NumberFormatException e) {
                    System.out.println("Ошибка: введите целое число.");
                }
            }
            case 14 -> {
                var count = service.callGetPublishersCount();
                System.out.println("Количество издателей: " + count);
            }
            default -> System.out.println("Неизвестная опция.");
        }
    }
}