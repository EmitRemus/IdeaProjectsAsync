import java.util.*;
import java.util.concurrent.*;

public class Main {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        int minRange = 0;
        int maxRange = 100;
        int arraySize = new Random().nextInt(21) + 40;

        int[] numbers = new int[arraySize];
        Random random = new Random();
        for (int i = 0; i < arraySize; i++) {
            numbers[i] = random.nextInt(maxRange - minRange + 1) + minRange;
        }

        long startTime = System.currentTimeMillis();

        int chunkSize = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(arraySize / chunkSize + 1);

        List<Future<Set<Integer>>> futures = new ArrayList<>();

        for (int i = 0; i < arraySize; i += chunkSize) {
            int[] chunk = new int[Math.min(chunkSize, arraySize - i)];
            System.arraycopy(numbers, i, chunk, 0, chunk.length);

            futures.add(executorService.submit(new PairProductTask(chunk)));
        }

        Set<Integer> resultSet = new CopyOnWriteArraySet<>();

        boolean allDone;
        do {
            allDone = true;
            for (Future<Set<Integer>> future : futures) {
                if (future.isDone()) {
                        resultSet.addAll(future.get());
                } else {
                    allDone = false;
                }
            }
        } while (!allDone);

        long endTime = System.currentTimeMillis();

        System.out.println("Оригінальний масив: " + Arrays.toString(numbers));
        System.out.println("Оброблений масив попарних добутків: " + resultSet);
        System.out.println("Час виконання програми: " + (endTime - startTime) + " мс");

        executorService.shutdown();
    }

    static class PairProductTask implements Callable<Set<Integer>> {
        private final int[] array;

        public PairProductTask(int[] array) {
            this.array = array;
        }

        @Override
        public Set<Integer> call() {
            Set<Integer> pairProducts = new CopyOnWriteArraySet<>();

            for (int i = 0; i < array.length - 1; i += 2) {
                int product = array[i] * array[i + 1];
                pairProducts.add(product);
            }
            return pairProducts;
        }
    }
}
