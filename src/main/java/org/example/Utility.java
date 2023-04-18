package org.example;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Utility {

    // Путь к папке exporters
    private static final String EXPORTERS_PATH = "./exporters";

    // Путь к папке input
    private static final String INPUT_PATH = "./input";

    // Путь к папке output
    private static final String OUTPUT_PATH = "./output";

    // Список экспортеров
    private static List<Exporter> exporters;

    public static void main(String[] args) throws Exception {
        // Инициализация списка экспортеров при старте
        exporters = getExporters();

        // Запуск планировщика задач на выполнение каждую минуту
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(Utility::processInputFolder, 0, 1, TimeUnit.MINUTES);
    }

    // Получение списка экспортеров из папки EXPORTERS_PATH
    private static List<Exporter> getExporters() throws Exception {
        List<Exporter> exporters = new ArrayList<>();

        // Получение списка файлов с расширением .dll из папки EXPORTERS_PATH
        File exportersFolder = new File(EXPORTERS_PATH);
        File[] files = exportersFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".dll"));

        // Загрузка каждой библиотеки в отдельном класслоадере и поиск экспортеров
        for (File file : files) {
            URLClassLoader classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()});
            Class<?>[] classes = getClasses(classLoader, file);
            for (Class<?> cls : classes) {
                if (Exporter.class.isAssignableFrom(cls)) {
                    Constructor<?> constructor = cls.getConstructor();
                    Exporter exporter = (Exporter) constructor.newInstance();
                    exporters.add(exporter);
                }
            }
        }

        return exporters;
    }

    // Получение списка классов из библиотеки
    private static Class<?>[] getClasses(URLClassLoader classLoader, File file) throws Exception {
        String fileName = file.getName();
        String className = fileName.substring(0, fileName.length() - 4);

        Class<?> cls = classLoader.loadClass(className);
        return cls.getDeclaredClasses();
    }

    // Обработка папки INPUT_PATH
    private static void processInputFolder() {
        try {
            // Получение списка файлов с расширением .txt из папки INPUT_PATH и ее подпапок
            List<File> files = new ArrayList<>();
            Files.walk(Paths.get(INPUT_PATH))
                    .filter(new Predicate<Path>() {
                        @Override
                        public boolean test(Path path) {
                            return Files.isRegularFile(path);
                        }
                    })
                    .filter(new Predicate<Path>() {
                        @Override
                        public boolean test(Path path) {
                            return path.toString().toLowerCase().endsWith(".txt");
                        }
                    })
                    .forEach(new Consumer<Path>() {
                        @Override
                        public void accept(Path path) {
                            files.add(path.toFile());
                        }
                    });

            // Сортировка файлов по названию
            Collections.sort(files, Comparator.comparing(File::getName));

            // Объединение содержимого всех файлов в один поток
            StringBuilder content = new StringBuilder();
            for (File file : files) {
                content.append(Files.readString(file.toPath()));
            }

            // Экспортирование содержимого в каждый из форматов
            for (Exporter exporter : exporters) {
                for (File file : files) {
                    try {
                        exporter.export(file);
                    } catch (ExporterException e) {
                        System.err.println("Exporter failed to export file: " + file.getName());
                        e.printStackTrace();
                    }
                }
            }

            // Перемещение обработанных файлов в папку output
            for (File file : files) {
                Path sourcePath = Paths.get(file.getPath());
                Path destinationPath = Paths.get(OUTPUT_PATH, file.getName());
                Files.move(sourcePath, destinationPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// В переменной content теперь содержится объединенное содержимое всех файлов
