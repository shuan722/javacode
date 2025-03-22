import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ParallelTextProcessor {
    // 预编译正则表达式
    private static final Pattern KEYWORD_PATTERN = Pattern.compile("总结|报告|摘要");

    // 结果存储结构（线程安全）
    private static final ConcurrentLinkedQueue<MatchResult> results =
            new ConcurrentLinkedQueue<>();

    // 匹配结果记录类
    private static class MatchResult {
        final String line;
        final Set<String> keywords;

        MatchResult(String line, Set<String> keywords) {
            this.line = line;
            this.keywords = keywords;
        }
    }

    // 单个文件处理方法
    private static void processFile(Path filePath) {
        try (Stream<String> lines = Files.lines(filePath)) {
            lines.forEach(line -> {
                Matcher matcher = KEYWORD_PATTERN.matcher(line);
                Set<String> matches = new LinkedHashSet<>();

                while (matcher.find()) {
                    matches.add(matcher.group());
                }

                if (!matches.isEmpty()) {
                    results.add(new MatchResult(line.trim(), matches));
                }
            });
        } catch (IOException e) {
            System.err.println("文件处理失败: " + filePath);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        // 1. 获取输入目录
        long startTime = System.currentTimeMillis();
        Path inputDir = Paths.get("excel_files");
        // 2. 创建线程池（建议核心数 = CPU逻辑核心数）
        int cores = Runtime.getRuntime().availableProcessors() * 2;
        ExecutorService executor = Executors.newFixedThreadPool(cores);
        // 3. 提交文件处理任务
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(inputDir, "*.txt")) {
            for (Path file : stream) {
                executor.submit(() -> processFile(file));
            }
        }
        // 4. 等待所有任务完成
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);
        // 5. 输出结果到文件
        Path outputFile = Paths.get("java_matches.txt");
        try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            results.forEach(result -> {
                try {
                    writer.write(result.line + "\t" +
                            String.join(",", result.keywords) + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        System.out.printf("处理完成，总耗时: %.2f秒\n",
                (System.currentTimeMillis() - startTime) / 1000.0);
    }

}
