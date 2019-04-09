package io.github.javafaktura.s01.e03.mock;

import java.util.Random;

import static java.time.LocalDate.now;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static spark.Spark.*;

public class CurrencyExchange {
    private static final Random rand = new Random();

    public static void start() {
        get("/*", (req, res) -> {
            MILLISECONDS.sleep(300);

            String date = req.pathInfo().replaceFirst("/", "");
            double plnRate = 4d + rand.nextDouble();

            return String.format("{\"base\":\"EUR\",\"rates\":{\"PLN\":%.4f},\"date\":\"%s\"}", plnRate, date);
        });
        awaitInitialization();
        System.out.println("Spark service listening on http://0.0.0.0:" + port());
        System.out.println(
                String.format("You can test it in your browser. Click here: http://localhost:%s/%s?symbols=PLN",
                        port(),
                        now().format(ISO_LOCAL_DATE)));
    }

    public static void main(String[] args) {
        start();
    }
}