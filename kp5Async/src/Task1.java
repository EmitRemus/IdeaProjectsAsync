import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Task1 {
    public static void main(String[] args) {
        CompletableFuture<String> fetchDataFromDb = CompletableFuture.supplyAsync(() -> {
            System.out.println("Getting data from database");
            simulateDelay();
            System.out.println("Data retrieved from DB.");
            return "(•ω•`)o";
        });
//
        CompletableFuture<String> processData = fetchDataFromDb.thenCompose(data -> {
            System.out.println("Processing retrieved data...");
            return CompletableFuture.supplyAsync(() -> {
                simulateDelay();
                String result = "This is a kaomoji: " + data;
                System.out.println("Data processed: " + result);
                return result;
            });
        });

        processData.thenAccept(result -> System.out.println("Final Result: " + result));

        sleep(5000);
    }

    private static void simulateDelay() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}