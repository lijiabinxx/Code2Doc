package com.company.code;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static String type;
    public static String paths;

    public static void main(String[] args) throws IOException {

        Properties properties = new Properties();
        properties.load(Main.class.getClassLoader().getResourceAsStream("config.properties"));
        paths = properties.getProperty("source");
        type = properties.getProperty("type");
        String[] split = paths.split(",");
        Arrays.stream(split).sequential().forEach(ele -> {
            try {
                File file = new File(ele);
                File target = new File(file.getParent(), file.getName() + "-output.doc");
                String outputPath = target.getPath();
                dealWithFile(ele, outputPath);
            } catch (IOException e) {
                if (e instanceof NoSuchFileException) {
                    System.out.println(e.getMessage() + "文件不存在");
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void dealWithFile(String path, String outputPath) throws IOException {
        try (Stream<Path> walk = Files.walk(Paths.get(path))) {

            List<String> result = walk.filter(Files::isRegularFile)
                    .map(Path::toString)
                    .filter(ele -> type.toLowerCase().contains(ele.split("\\.")[ele.split("\\.").length - 1]))
                    .filter(ele -> {
                        try {
                            //过滤空文件
                            return Files.lines(Paths.get(ele)).count() != 0;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return false;
                    })
                    .filter(ele -> {
                        try {
                            //过滤大文件
                            return Files.size(Paths.get(ele)) < 10 * 1024;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return false;
                    })
                    //过滤相关文件夹
                    .filter(ele -> !ele.contains("common") && !ele.contains("node_modules") && !ele.contains("resources") && !ele.contains("static"))
                    .collect(Collectors.toList());

            if (result.size() == 0) {
                System.out.println("没有检索到" + type + "相关文件");
                return;
            }
            result.stream().sequential().forEach(ele -> {
                try {
                    String[] split = ele.split("\\\\");
                    String fileName = split[split.length - 1];
                    Files.write(Paths.get(outputPath), ("\n文件名: " + fileName + "\n").getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    Files.write(Paths.get(outputPath), IOUtils.readLines(new FileReader(ele)), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            System.out.println("word生成成功，文件路径为:" + outputPath);
        }
    }
}
