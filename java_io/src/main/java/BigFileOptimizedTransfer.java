import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;

public class BigFileOptimizedTransfer {

    private static final String srcFile = "E:\\model_files\\deepseek-ai\\DeepSeek-R1-Distill-Qwen-7B\\model-00001-of-000002.safetensors";
    private static final String destFile = "E:\\javacode\\java_io\\src\\main\\resources\\000002.safetensors";
    private static final int BUFFER_SIZE = 64 * 1024; // 64KB缓冲区
    private static final long PROGRESS_INTERVAL = 1024 * 1024; // 每1MB更新进度

    public static void main(String[] args) {
        Path src = Paths.get(srcFile);
        Path dest = Paths.get(destFile);
        try {
            // 获取准确文件大小
            long totalSize = Files.size(src);
            long copied = 0;
            long lastReport = 0;

            try (FileChannel inChannel = FileChannel.open(src, StandardOpenOption.READ);
                 FileChannel outChannel = FileChannel.open(dest,
                         StandardOpenOption.CREATE,
                         StandardOpenOption.WRITE,
                         StandardOpenOption.TRUNCATE_EXISTING)) {

                // 使用内存映射提升大文件性能
                if (totalSize > 1024 * 1024 * 1024) { // 超过1GB使用直接传输
                    long position = 0;
                    while (position < totalSize) {
                        position += inChannel.transferTo(position, BUFFER_SIZE, outChannel);
                        reportProgress(position, totalSize);
                    }
                } else { // 中小文件使用缓冲复制
                    ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
                    while (inChannel.read(buffer) != -1) {
                        buffer.flip();
                        outChannel.write(buffer);
                        buffer.clear();

                        copied += BUFFER_SIZE;
                        if (copied - lastReport >= PROGRESS_INTERVAL) {
                            reportProgress(copied, totalSize);
                            lastReport = copied;
                        }
                    }
                }
            }
            System.out.println("\n传输完成！");
        } catch (IOException e) {
            handleException(e);
        }
    }

    private static void reportProgress(long current, long total) {
        double percent = (current * 100.0) / total;
        System.out.printf("\r当前进度: %.2f%% (%d/%d MB)",
                percent,
                current / 1024 / 1024,
                total / 1024 / 1024);
    }

    private static void handleException(IOException e) {
        if (e instanceof FileSystemException) {
            System.err.println("文件系统错误: " + e.getMessage());
        } else {
            System.err.println("传输错误: " + e.getClass().getSimpleName());
            e.printStackTrace();
        }
    }
}