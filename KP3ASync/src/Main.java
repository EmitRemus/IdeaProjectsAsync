import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.Random;


public class Main {

    static class matrixsearch extends RecursiveTask<Integer> {
        private final int[][] array;
        private final int startRow, endRow;

        public matrixsearch(int[][] array, int startRow, int endRow) {
            this.array = array;
            this.startRow = startRow;
            this.endRow = endRow;
        }

        @Override
        protected Integer compute() {
            int foundValue = -1;
            if (endRow - startRow <= 10) {
                for (int i = startRow; i < endRow; i++) {
                    for (int j = 0; j < array[i].length; j++) {
                        if (array[i][j] == i + j) {
                            return array[i][j];
                        }
                    }
                }
                return foundValue;
            } else {
                int mid = (startRow + endRow) / 2;
                matrixsearch leftTask = new matrixsearch(array, startRow, mid);
                matrixsearch rightTask = new matrixsearch(array, mid, endRow);

                leftTask.fork();
                int rightResult = rightTask.compute();
                int leftResult = leftTask.join();

                return leftResult != -1 ? leftResult : rightResult;
            }
        }
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter number of rows: ");
        int rows = scanner.nextInt();
        System.out.println("Enter number of columns: ");
        int cols = scanner.nextInt();
        System.out.println("Enter minimum random value: ");
        int minVal = scanner.nextInt();
        System.out.println("Enter maximum random value: ");
        int maxVal = scanner.nextInt();

        int[][] array = new int[rows][cols];
        Random rand = new Random();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                array[i][j] = rand.nextInt(maxVal - minVal + 1) + minVal;
            }
        }

        System.out.println("Generated Array:");
        for (int[] row : array) {
            for (int val : row) {
                System.out.print(val + " ");
            }
            System.out.println();
        }

        // Work Stealing
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        matrixsearch task = new matrixsearch(array, 0, rows);

        long startTime = System.nanoTime();
        int resultWorkStealing = forkJoinPool.invoke(task);
        long endTime = System.nanoTime();

        System.out.println("Work Stealing Result: " + resultWorkStealing);
        System.out.println("Work Stealing Time: " + (endTime - startTime) + " ms");

        // Work Dealing
        ExecutorService executor = Executors.newFixedThreadPool(4);
        int chunkSize = rows / 4;


        List<Callable<Integer>> tasks = new ArrayList<>();

        startTime = System.nanoTime();
        for (int i = 0; i < rows; i += chunkSize) {
            int startRow = i;
            int endRow = Math.min(i + chunkSize, rows);

            Callable<Integer> searchTask = () -> {
                for (int row = startRow; row < endRow; row++) {
                    for (int col = 0; col < cols; col++) {
                        if (array[row][col] == row + col) {
                            return array[row][col]; // Return result if found
                        }
                    }
                }
                return -1; // Return -1 if not found in this chunk
            };

            tasks.add(searchTask);
        }


        List<Future<Integer>> results = executor.invokeAll(tasks);
        int resultWorkDealing = -1;

        for (Future<Integer> future : results) {
            resultWorkDealing = future.get();
            if (resultWorkDealing != -1) {break;}
        }

        endTime = System.nanoTime();
        executor.shutdown();

        System.out.println("Work Dealing Result>>> " + resultWorkDealing);
        System.out.println("Work Dealing Time>>> " + (endTime - startTime) + " ms");

    }
}
