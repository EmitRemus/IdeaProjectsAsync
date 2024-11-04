import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Main {

    public static List<Integer> createArray(int minSize, int maxSize, int minValue, int maxValue) {
        Random r = new Random();
        int size = r.nextInt(maxSize - minSize + 1) + minSize;
        List<Integer> array = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            array.add(r.nextInt(maxValue - minValue + 1) + minValue);
        }
        return array;
    }

    public static void writeToFile(List<Integer> array, String path) throws IOException {
        Files.write(Paths.get(path), array.stream().map(String::valueOf).collect(Collectors.toList()));
    }

    public static List<Integer> readFromFile(String path) throws IOException {
        return Files.lines(Paths.get(path)).map(Integer::parseInt).collect(Collectors.toList());
    }

    static class arrayOperation extends RecursiveTask<List<Integer>> {
        private final List<Integer> array;
        private final String operation;

        arrayOperation(List<Integer> array, String operation) {
            this.array = array;
            this.operation = operation;
        }

        @Override
        protected List<Integer> compute() {
            List<Integer> result = switch (operation) {
                case "multiply" -> array.stream().map(x -> x * 3).sorted().collect(Collectors.toList());
                case "even" -> array.stream().filter(x -> x % 2 == 0).sorted().collect(Collectors.toList());
                case "range" -> array.stream().filter(x -> x >= 10 && x <= 175).sorted().collect(Collectors.toList());
                default -> new ArrayList<>();
            };
            return result;
        }

    }
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        List<Integer> array1 = createArray(15, 25, 0, 1000);
        List<Integer> array2 = createArray(15, 25, 0, 1000);
        List<Integer> array3 = createArray(15, 25, 0, 1000);

        writeToFile(array1, "array1.txt");
        writeToFile(array2, "array2.txt");
        writeToFile(array3, "array3.txt");

        List<Integer> ffarray1 = readFromFile("array1.txt");
        List<Integer> ffarray2 = readFromFile("array2.txt");
        List<Integer> ffarray3 = readFromFile("array3.txt");

        ExecutorService exService = Executors.newFixedThreadPool(3);

        ForkJoinPool forkJoinPool = new ForkJoinPool();

        Future<List<Integer>> futureArray1 = exService.submit(() -> forkJoinPool.invoke(new arrayOperation(ffarray1, "multiply")));
        Future<List<Integer>> futureArray2 = exService.submit(() -> forkJoinPool.invoke(new arrayOperation(ffarray2, "even")));
        Future<List<Integer>> futureArray3 = exService.submit(() -> forkJoinPool.invoke(new arrayOperation(ffarray3, "range")));

        List<Integer> resultArray = futureArray3.get();
        resultArray.removeAll(futureArray2.get());
        resultArray.removeAll(futureArray1.get());

        resultArray.sort(Comparator.naturalOrder());

        System.out.println("result = " + resultArray);
        exService.shutdown();
    }
}