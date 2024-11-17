import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

public class task2 {


    static class DirectorySearch extends RecursiveTask<List<File>> {
        private final File directory;
        private final List<String> imageExtensions;

        public DirectorySearch(File directory, List<String> imageExtensions) {
            this.directory = directory;
            this.imageExtensions = imageExtensions;
        }

        @Override
        protected List<File> compute() {
            List<File> images = new ArrayList<>();
            List<DirectorySearch> subTasks = new ArrayList<>();

            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        DirectorySearch subTask = new DirectorySearch(file, imageExtensions);
                        subTask.fork();
                        subTasks.add(subTask);
                    } else if (isImage(file)) {
                        images.add(file);
                    }
                }
            }

            for (DirectorySearch task : subTasks) {
                images.addAll(task.join());
            }

            return images;
        }

        private boolean isImage(File file) {
            String name = file.getName().toLowerCase();
            return imageExtensions.stream().anyMatch(name::endsWith);
        }
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the directory path: ");
        String dirPath = scanner.nextLine();

        File directory = new File(dirPath);
        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("Invalid directory path.");
            return;
        }

        List<String> imageExtensions = List.of(".jpg", ".jpeg", ".png", ".gif");

        ForkJoinPool forkJoinPool = new ForkJoinPool();
        DirectorySearch task = new DirectorySearch(directory, imageExtensions);

        long startTime = System.currentTimeMillis();
        List<File> images = forkJoinPool.invoke(task);
        long endTime = System.currentTimeMillis();

        System.out.println("Found " + images.size() + " images.");
        System.out.println("Execution Time>>> " + (endTime - startTime) + " ms");

        if (!images.isEmpty()) {
            File lastImage = images.get(images.size() - 1);
            System.out.println("Opening the last image>>> " + lastImage.getAbsolutePath());
            Desktop.getDesktop().open(lastImage);
        } else {
            System.out.println("No images found in the directory.");
        }
    }
}
