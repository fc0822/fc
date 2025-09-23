import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Pattern;

public class Main {
    // 改进的标点符号正则表达式，更全面地处理中英文标点
    private static final Pattern PUNCTUATION_PATTERN = Pattern.compile("[\\p{P}\\s]+");

    // 中文分词的简单实现（可替换为更专业的分词库）
    private static List<String> segmentChinese(String text) {
        List<String> words = new ArrayList<>();
        // 基本单字分词（实际应用中建议使用IKAnalyzer等专业库）
        for (char c : text.toCharArray()) {
            if (!Character.isWhitespace(c) && !PUNCTUATION_PATTERN.matcher(String.valueOf(c)).matches()) {
                words.add(String.valueOf(c));
            }
        }
        return words;
    }

    // 英文分词（按空格分割并提取有意义的单词）
    private static List<String> segmentEnglish(String text) {
        List<String> words = new ArrayList<>();
        String[] tokens = text.split("\\s+");
        for (String token : tokens) {
            if (!token.isEmpty()) {
                words.add(token);
            }
        }
        return words;
    }

    public static String readFile(String filePath) throws IOException {
        // 使用UTF-8编码读取文件，确保中文正确处理
        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        return new String(bytes, "UTF-8") + "\n";
    }

    static String preprocessText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // 转换为小写
        String lowerText = text.toLowerCase();

        // 替换标点符号为空格
        String processed = PUNCTUATION_PATTERN.matcher(lowerText).replaceAll(" ");

        // 合并多个空格为一个
        return processed.replaceAll("\\s+", " ").trim();
    }

    public static double calculateSimilarity(String text1, String text2) {
        // 预处理文本
        String processed1 = preprocessText(text1);
        String processed2 = preprocessText(text2);

        // 处理空文本情况
        if (processed1.isEmpty() || processed2.isEmpty()) {
            return 0.0;
        }

        // 分词（中英文分别处理）
        List<String> words1 = segmentText(processed1);
        List<String> words2 = segmentText(processed2);

        // 计算词频
        Map<String, Integer> frequencyMap1 = getFrequencyMap(words1);
        Map<String, Integer> frequencyMap2 = getFrequencyMap(words2);

        // 获取所有独特的词
        Set<String> allWords = new HashSet<>(frequencyMap1.keySet());
        allWords.addAll(frequencyMap2.keySet());

        // 计算向量点积和模长
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (String word : allWords) {
            int count1 = frequencyMap1.getOrDefault(word, 0);
            int count2 = frequencyMap2.getOrDefault(word, 0);

            dotProduct += count1 * count2;
            norm1 += count1 * count1;
            norm2 += count2 * count2;
        }

        // 避免除以零
        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        // 计算余弦相似度
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    // 根据文本内容自动选择合适的分词策略
    private static List<String> segmentText(String text) {
        // 判断文本是否包含中文
        if (text.matches(".*[\\u4e00-\\u9fa5].*")) {
            return segmentChinese(text);
        } else {
            return segmentEnglish(text);
        }
    }

    private static Map<String, Integer> getFrequencyMap(List<String> words) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        for (String word : words) {
            frequencyMap.put(word, frequencyMap.getOrDefault(word, 0) + 1);
        }
        return frequencyMap;
    }

    public static void writeResult(String filePath, double similarity) throws IOException {
        // 格式化输出为百分比，保留两位小数
        DecimalFormat df = new DecimalFormat("0.00%");
        String result = df.format(similarity);
        Files.write(Paths.get(filePath), result.getBytes("UTF-8"));
    }

    public static void main(String[] args) {
        // 验证参数
        if (args.length != 3) {
            System.err.println("参数错误！正确格式：java -jar main.jar [原文路径] [抄袭文路径] [结果路径]");
            return;
        }

        try {
            // 读取文件内容
            String origContent = readFile(args[0]);
            String copyContent = readFile(args[1]);

            // 计算相似度
            double similarity = calculateSimilarity(origContent, copyContent);

            // 写入结果
            writeResult(args[2], similarity);
            System.out.println("查重完成");

        } catch (IOException e) {
            System.err.println("处理文件时发生错误：" + e.getMessage());
            e.printStackTrace();
        }
    }
}