import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MergeFiles {
    public static void main(String[] args) {
        // Получаем имя текущей папки для имени выходного файла
        String currentFolder = Paths.get("").toAbsolutePath().getFileName().toString();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String outputFile = String.format("merged_%s_%s.txt", currentFolder, timestamp);
        
        // Можно указать конкретные расширения (оставить пустым для ВСЕХ файлов)
        List<String> extensions = List.of(); // пустой список = все файлы
        
        // Список папок и файлов для исключения
        List<String> excludeDirs = List.of("target", ".git", ".idea", "node_modules", "build", "dist");
        List<String> excludeFiles = List.of(
            outputFile,  // не включать сам выходной файл
            "merged_code.txt",  // старые мержи
            "MergeFiles.java",  // сам исходник
            "MergeFiles.class"  // скомпилированный класс
        );
        
        // Расширения бинарных файлов (их не будем читать как текст)
        List<String> binaryExtensions = List.of(
            ".class", ".jar", ".war", ".ear",
            ".png", ".jpg", ".jpeg", ".gif", ".bmp", ".ico",
            ".pdf", ".doc", ".docx", ".xls", ".xlsx",
            ".zip", ".tar", ".gz", ".7z", ".rar",
            ".exe", ".dll", ".so", ".dylib",
            ".mp3", ".mp4", ".avi", ".mov",
            ".db", ".lock", ".log"
        );

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFile))) {
            
            // Заголовок файла
            writer.write("=".repeat(80));
            writer.newLine();
            writer.write("СВОДНЫЙ ФАЙЛ КОДА ПРОЕКТА");
            writer.newLine();
            writer.write("Папка: " + Paths.get("").toAbsolutePath());
            writer.newLine();
            writer.write("Создан: " + LocalDateTime.now());
            writer.newLine();
            writer.write("=".repeat(80));
            writer.newLine();
            writer.newLine();
            
            final int[] fileCount = {0};
            final int[] skippedCount = {0};
            
            Files.walk(Paths.get("."))
                    .filter(Files::isRegularFile)
                    .filter(path -> shouldInclude(path, excludeDirs, excludeFiles, extensions))
                    .forEach(path -> {
                        try {
                            // Проверка на бинарный файл
                            if (isBinaryFile(path, binaryExtensions)) {
                                writer.write("=".repeat(80));
                                writer.newLine();
                                writer.write("Файл: " + path.toAbsolutePath());
                                writer.newLine();
                                writer.write("⚠️ [БИНАРНЫЙ ФАЙЛ - пропущен]");
                                writer.newLine();
                                writer.write("=".repeat(80));
                                writer.newLine();
                                writer.newLine();
                                writer.newLine();
                                skippedCount[0]++;
                                return;
                            }
                            
                            // Записать заголовок с именем файла
                            writer.write("=".repeat(80));
                            writer.newLine();
                            writer.write("Файл: " + path.toAbsolutePath());
                            writer.newLine();
                            writer.write("-".repeat(40));
                            writer.newLine();
                            
                            // Записать содержимое
                            boolean hasContent = false;
                            for (String line : Files.readAllLines(path)) {
                                writer.write(line);
                                writer.newLine();
                                hasContent = true;
                            }
                            
                            if (!hasContent) {
                                writer.write("[ФАЙЛ ПУСТ]");
                                writer.newLine();
                            }
                            
                            writer.write("=".repeat(80));
                            writer.newLine();
                            writer.newLine();
                            writer.newLine();
                            
                            fileCount[0]++;
                            
                            // Прогресс в консоль
                            if (fileCount[0] % 50 == 0) {
                                System.out.println("Обработано файлов: " + fileCount[0]);
                            }
                            
                        } catch (IOException e) {
                            System.err.println("Ошибка при чтении файла: " + path);
                            e.printStackTrace();
                        }
                    });
            
            // Итоговая статистика
            writer.write("=".repeat(80));
            writer.newLine();
            writer.write("СТАТИСТИКА СБОРА");
            writer.newLine();
            writer.write("=".repeat(80));
            writer.newLine();
            writer.write("Обработано текстовых файлов: " + fileCount[0]);
            writer.newLine();
            writer.write("Пропущено бинарных файлов: " + skippedCount[0]);
            writer.newLine();
            writer.write("Всего найдено файлов: " + (fileCount[0] + skippedCount[0]));
            writer.newLine();
            writer.write("=".repeat(80));
            
            System.out.println("\n✅ ГОТОВО!");
            System.out.println("📁 Текстовых файлов собрано: " + fileCount[0]);
            System.out.println("🚫 Бинарных файлов пропущено: " + skippedCount[0]);
            System.out.println("📄 Результат сохранён в: " + outputFile);
            
        } catch (IOException e) {
            System.err.println("Критическая ошибка при создании выходного файла:");
            e.printStackTrace();
        }
    }
    
    /**
     * Проверяет, нужно ли включать файл
     */
    private static boolean shouldInclude(Path path, List<String> excludeDirs, 
                                          List<String> excludeFiles, List<String> extensions) {
        String pathStr = path.toString().replace("\\", "/");
        
        // Пропустить исключённые папки
        for (String excludeDir : excludeDirs) {
            if (pathStr.contains("/" + excludeDir + "/") || pathStr.startsWith(excludeDir + "/")) {
                return false;
            }
        }
        
        // Пропустить исключённые файлы
        String fileName = path.getFileName().toString();
        for (String excludeFile : excludeFiles) {
            if (fileName.equals(excludeFile)) {
                return false;
            }
        }
        
        // Если расширения не указаны - включаем все файлы
        if (extensions.isEmpty()) {
            return true;
        }
        
        // Проверка расширения
        String ext = getFileExtension(fileName).toLowerCase();
        return extensions.contains(ext);
    }
    
    /**
     * Проверяет, является ли файл бинарным
     */
    private static boolean isBinaryFile(Path path, List<String> binaryExtensions) {
        String ext = getFileExtension(path.getFileName().toString()).toLowerCase();
        
        // Проверка по расширению
        if (binaryExtensions.contains(ext)) {
            return true;
        }
        
        // Дополнительная проверка: читаем первые 1024 байта
        try (InputStream in = Files.newInputStream(path)) {
            byte[] buffer = new byte[1024];
            int bytesRead = in.read(buffer);
            if (bytesRead > 0) {
                for (int i = 0; i < bytesRead; i++) {
                    byte b = buffer[i];
                    // Если много нулевых байтов или непечатаемых символов (>30%)
                    if (b == 0) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            return true; // Если не удалось прочитать - считаем бинарным
        }
        
        return false;
    }
    
    /**
     * Получает расширение файла
     */
    private static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) {
            return "";
        }
        return fileName.substring(lastDot);
    }
}