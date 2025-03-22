import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class File_ {


    public static void main(String[] args) throws IOException {
        String filePath = "demo01.txt";
        File file = new File(filePath);
        createNewFile(file);
        System.out.println(fileInfo(file));
        readFile(file);
        String SourcePath = "excel_files\\2024-03-20.xlsx";
        String DestinationPath = "excel_files\\2024-03-20_bak.xlsx";
        fileCopy(SourcePath, DestinationPath);
        String propertiesFile = "java_io\\src\\main\\resources\\config.properties";
        propertiesUsage(propertiesFile);
    }


    /**
     * properties类操作
     */
    private static void propertiesUsage(String file) {
        try {
            // 创建Properties对象
            Properties properties = new Properties();
            //加载指定配置文件
            properties.load(new FileReader(file));
            // 把k-v显示控制台
            properties.list(System.out);
            System.out.println("============================");
            // 根据课可以获取对于的值
            String user = properties.getProperty("user");
            String pwd = properties.getProperty("pwd");
            System.out.println("用户名=" + user);
            System.out.println("密码=" + pwd);

            properties.setProperty("user", "汤姆猫"); // 注意保存时中文变成unicode编码
            properties.store(Files.newOutputStream(Paths.get(file)), null);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 简单二进制文件拷贝
     *
     * @param srcPath  源文件
     * @param destPath 目标文件
     */
    private static void fileCopy(String srcPath, String destPath) {
        byte[] buffer = new byte[1024];
        int readLen = 0;
        try {
            try (FileInputStream fileInputStream = new FileInputStream(srcPath);
                 FileOutputStream fileOutputStream = new FileOutputStream(destPath)) {
                while ((readLen = fileInputStream.read(buffer)) != -1) {
                    // 边读边写
                    fileOutputStream.write(buffer, 0, readLen);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    /**
     * 字节流文件读取通用模式，引入缓存。
     *
     * @param file
     * @throws IOException
     */
    private static void readFile(File file) throws IOException {
        byte[] buffer = new byte[1024];
        int readLen = 0;
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            while ((readLen = fileInputStream.read(buffer)) != -1) {
                System.out.println(new String(buffer, 0, readLen));
            }
        }
    }

    /**
     * 显示文件信息： 文件名， 文件地址， 上级目录， 文件大小， 文件或者目录
     *
     * @param file 文件对象
     * @return
     */
    private static String fileInfo(File file) {
        return "{filename='" + file.getName() + '\'' +
                ", path='" + file.getAbsoluteFile() + '\'' +
                ", parentPath='" + file.getParent() + '\'' +
                ", length='" + file.length() + '\'' +
                ", isFile='" + file.isFile() + '\'' +
                ", isDirectory='" + file.isDirectory() + '\'' +
                '}';
    }

    /**
     * 创建空白文件
     *
     * @param file 文件对象
     */
    private static void createNewFile(File file) {
        if (file.exists()) {
            System.out.println(file.getAbsoluteFile() + "文件已存在，无需重新创建");
            return;
        }
        // 创建新文件
        try {
            if (file.createNewFile()) {
                System.out.println(file.getAbsoluteFile() + " 文件创建成功");
            } else {
                System.out.println(file.getAbsoluteFile() + "文件创建失败");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("文件创建异常");
        }
    }
}
