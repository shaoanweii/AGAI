package com.voc.service.common.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonlSplitterUtils {

        private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB

        public static void main(String[] args) throws IOException {
//            String inputFilePath = "path/to/your/input.jsonl"; // 替换为你的JSONL文件路径
//            splitJsonlFile(inputFilePath);

            String input = "dsfsf\"custom_id\":\"2cbb1e51644638a81da72359e81898d5\",\"method\":\"POST\",\"url\":\"/v4/chat/completions\",\"body\":{\"model\":\"glm-4-flash\",\"messages\":[{\"role\":\"system\",\"content\":\"你需要扮演一个汽车行业文本信息抽取器，可以针对文本观点的各个元素进行全面且准确的抽取，多对多的情况需要全部展开，只输出我要求的分析结果，不要多余的解释文字\"},{\"role\":\"user\",\"content\":\"\\n# CONTEXT #\\n我会提供文本[TEXT]\\n###########\\n# OBJECTIVE #\\n我希望你针对我提供的文本[TEXT]，进行信息提取，遵循以下步骤：1. 识别所有提到的车型，包括汽车的品牌vehicle_brand以及对应的车系vehicle_model，汽车品牌和车系必须在[TEXT]中出现,没有提及则默认为NA,汽车品牌比如上汽大众,奇瑞,长城等,汽车系列比如model 3,A6L,途观L Pro等;\\n2. 针对每一个评价的车型，识别该车型所有观点评价[VIEWPOINTS]，其中每个观点评价由六元组构成，包含以下元素,每个维度输出在10个字之内;\\n    SCENARIO:观点描述的用车场景，例如用车的天气、时速、操作下发生的用车场景，越野、自驾等生活场景中选取一种,没有明确提及则默认为NA;\\n    SUBJECT: 观点针对的对象主体，例如汽车的位置(尾部/前脸等)/部件(大灯/车门/轮胎等)/配置(车载大屏/天窗/激光雷达等)/功能(智能驾驶/蓝牙车载电话等)/尺寸(车身尺寸/轮胎尺寸等)等，销售/售后/权益服务等中选择一种，默认为整车;\\n    ASPECT: 观点对象的属性或者方面;\\n    DESC: 观点对应对象/对象属性或者方面的评论描述词;\\n    SENTIMENT: 观点所表达的情感意图，返回正向、负向、中性之一;\\n    INTENT: 观点所表达的意图,返回投诉、抱怨、咨询、其他之一;\\n    CONFIDENCE: 观点的情感的置信度，用来表示情感的程度，-1到1 之间，数字越大正向程度越高，数字越小负向程度越高;\\n注意：观点评价六元组要全面，不要有遗漏;\\n###########\\n# RESPONSE: JSON FORMAT #\"}";

            // 正则表达式来匹配custom_id的值，可以是任意字符直到遇到闭合的双引号
            String regex = "\"custom_id\":\"([^\"]*)\"";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(input);

            if (matcher.find()) {
                // 提取匹配到的第一个custom_id的值
                String customId = matcher.group(1);
                System.out.println("Extracted custom_id: " + customId);
            } else {
                System.out.println("No custom_id found in the input string.");
            }
        }
      public  static String getCustomId(String jsonlLine) {
            // 正则表达式来匹配custom_id的值，可以是任意字符直到遇到闭合的双引号
            String regex = "\"custom_id\":\"([^\"]*)\"";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(jsonlLine);

            if (matcher.find()) {
                // 提取匹配到的第一个custom_id的值
                String customId = matcher.group(1);
               return customId;
            } else {
             return null;
            }
        }


    public static Map<List<String>, Path> splitJsonlFile(String inputFilePath) throws IOException {
        Path inputPath = Paths.get(inputFilePath);
        Map<List<String>, Path> paths = new HashMap<>();
        int fileCount = 1;
        String baseOutputFileName = inputFilePath.replace(".jsonl", "");
        Path outputPath = Paths.get(baseOutputFileName + "_" + fileCount + ".jsonl");
        BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8);
        long currentFileSize = 0;
        int lineCount = 0;
        List<String> customIds = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(inputPath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                long lineSize = line.getBytes(StandardCharsets.UTF_8).length + System.lineSeparator().getBytes(StandardCharsets.UTF_8).length;
                if (currentFileSize + lineSize > MAX_FILE_SIZE) {
                    writer.close(); // Close current file
                    paths.put(customIds, outputPath); // Record the path and line count of the current file
                    fileCount++; // Increment file count
                    outputPath = Paths.get(baseOutputFileName + "_" + fileCount + ".jsonl");
                    writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8); // Create new file
                    currentFileSize = 0; // Reset file size
                    customIds = new ArrayList<>();
                    lineCount = 0; // Reset line count for the new file
                }
                if (getCustomId(line) != null) {
                    customIds.add(getCustomId(line));
                }
                writer.write(line);
                writer.newLine();
                currentFileSize += lineSize;
                lineCount++;
            }
        } finally {
            writer.close(); // Ensure the last writer is closed
        }
        // Record the path and line count of the last file
        paths.put(customIds, outputPath);
        return paths;
    }

    }
