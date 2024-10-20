import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;

// CoffeeShop class to manage the shop status and orders
class CoffeeShop {
    private boolean open;
    private final Semaphore orderSemaphore;
    private final int maxConcurrentOrders;
    private final BlockingQueue<String> orderQueue;

    public CoffeeShop(int maxConcurrentOrders) {
        this.maxConcurrentOrders = maxConcurrentOrders;
        this.orderSemaphore = new Semaphore(maxConcurrentOrders);
        this.open = true;
        this.orderQueue = new LinkedBlockingQueue<>();  // Queue for customer orders
    }

    public synchronized boolean isOpen() {
        return open;
    }

    public synchronized void closeShop() {
        open = false;
        System.out.println("The coffee shop is now closed. No more customers can enter.");
    }

    public Semaphore getOrderSemaphore() {
        return orderSemaphore;
    }

    public BlockingQueue<String> getOrderQueue() {
        return orderQueue;
    }
}

class Barista implements Runnable {
    private final CoffeeShop shop;
    private final ExecutorService baristaExecutor;
    int MAXORDERS = 2;

    public Barista(CoffeeShop shop) {
        this.shop = shop;
        this.baristaExecutor = Executors.newFixedThreadPool(MAXORDERS);
    }

    @Override
    public void run() {
        System.out.println("Barista is ready to make coffee.");
        while (shop.isOpen() || !shop.getOrderQueue().isEmpty()) {
            try {
                // Wait for an order to be placed in the queue
                String customer = shop.getOrderQueue().poll(1, TimeUnit.SECONDS);  // Wait if the queue is empty
                if (customer != null) {
                    // Submit a task to the barista's thread pool
                    baristaExecutor.submit(() -> prepareCoffee(customer));
                }
            } catch (InterruptedException e) {
                System.out.println("Barista was interrupted during coffee preparation.");
            }
        }

        // Shutdown the executor once all tasks are complete
        baristaExecutor.shutdown();
        try {
            if (!baristaExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                System.out.println("Barista took too long to finish, forcing shutdown...");
            }
        } catch (InterruptedException e) {
            System.out.println("Barista shutdown interrupted.");
        }

        System.out.println("Barista is done for the day and is leaving.");
    }


    private void prepareCoffee(String customer) {
        try {
            System.out.println("Barista is preparing coffee for " + customer + "...");
            Thread.sleep(7000);
            System.out.println("Barista finished preparing coffee for " + customer + ".");
        } catch (InterruptedException e) {
            System.out.println("Barista was interrupted while preparing coffee for " + customer);
        } finally {
            shop.getOrderSemaphore().release();
        }
    }
}

// Customer class representing a customer ordering coffee
class Customer implements Runnable {
    private final CoffeeShop shop;
    private final String customerName;

    public Customer(CoffeeShop shop, String customerName) {
        this.shop = shop;
        this.customerName = customerName;
    }

    @Override
    public void run() {
        if (shop.isOpen()) {
            System.out.println(customerName + " enters the shop and wants to order coffee.");
            try {
                shop.getOrderSemaphore().acquire();

                if (shop.isOpen()) {
                    System.out.println(customerName + " orders coffee.");
                    shop.getOrderQueue().put(customerName);
                } else {
                    System.out.println(customerName + " could not order coffee as the shop has closed.");
                    shop.getOrderSemaphore().release();
                }
            } catch (InterruptedException e) {
                System.out.println(customerName + " was interrupted while placing an order.");
            }
        } else {
            System.out.println(customerName + " tries to enter but the shop is closed.");
        }
    }
}

public class Main {

    public static void main(String[] args) {

        CoffeeShop shop = new CoffeeShop(2);


        Thread baristaThread = new Thread(new Barista(shop));
        baristaThread.start();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(shop::closeShop, 10, TimeUnit.SECONDS);  // Close the shop after 10 seconds

        List<Thread> customerThreads = new ArrayList<>();

        int customerCounter = 1;

        while (shop.isOpen()) {
            String customerName = "Customer " + customerCounter;
            Thread customerThread = new Thread(new Customer(shop, customerName));
            customerThreads.add(customerThread);
            customerThread.start();
            customerCounter++;

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                System.out.println("Error in customer arrival timing.");
            }
        }

        for (Thread customerThread : customerThreads) {
            try {
                customerThread.join(); // Wait for all customers in threads to finish
            } catch (InterruptedException e) {
                System.out.println("Error waiting for customer threads to finish.");
            }
        }

        scheduler.shutdown();

        try {
            baristaThread.join();  // Wait for the barista to finish
        } catch (InterruptedException e) {
            System.out.println("Error waiting for barista to finish.");
        }

        System.out.println("Coffee shop simulation completed.");
    }
}
