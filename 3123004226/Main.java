import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        // 验证命令行参数数量
        if (args.length != 3) {
            System.err.println("参数错误！正确格式：java -jar main.jar [原文路径] [抄袭文路径] [结果路径]");
            return;
        }

        String origPath = args[0];    // 原文文件路径
        String copyPath = args[1];    // 抄袭版文件路径
        String resultPath = args[2];  // 结果输出路径

        try {
            // 读取文件内容
            String origText = readFile(origPath);
            String copyText = readFile(copyPath);

            // 计算相似度（余弦相似度算法）
            double similarity = calculateSimilarity(origText, copyText);

            // 写入结果（保留两位小数）
            writeResult(resultPath, similarity);

        } catch (IOException e) {
            System.err.println("文件操作失败：" + e.getMessage());
        }
    }

    // 读取文件内容为字符串
    static String readFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n"); // 保留换行符拼接内容
            }
        }
        return content.toString();
    }

    // 计算文本相似度（余弦相似度）
    static double calculateSimilarity(String text1, String text2) {
        // 预处理：去除标点符号、转换为小写，统一格式
        String processed1 = text1.replaceAll("[^a-zA-Z0-9\u4e00-\u9fa5]", " ").toLowerCase();
        String processed2 = text2.replaceAll("[^a-zA-Z0-9\u4e00-\u9fa5]", " ").toLowerCase();

        // 分词（按空格分割，过滤空字符串）
        String[] words1 = processed1.split("\\s+");
        String[] words2 = processed2.split("\\s+");

        // 构建词袋（包含所有出现的词，确保向量维度一致）
        Set<String> wordSet = new HashSet<>();
        for (String word : words1) {
            if (!word.isEmpty()) wordSet.add(word);
        }
        for (String word : words2) {
            if (!word.isEmpty()) wordSet.add(word);
        }

        // 生成词频向量
        Map<String, Integer> vec1 = new HashMap<>();
        Map<String, Integer> vec2 = new HashMap<>();
        for (String word : words1) {
            if (!word.isEmpty()) vec1.put(word, vec1.getOrDefault(word, 0) + 1);
        }
        for (String word : words2) {
            if (!word.isEmpty()) vec2.put(word, vec2.getOrDefault(word, 0) + 1);
        }

        // 计算余弦相似度：点积 / (模长1 * 模长2)
        double dotProduct = 0;
        double norm1 = 0;
        double norm2 = 0;
        for (String word : wordSet) {
            int count1 = vec1.getOrDefault(word, 0);
            int count2 = vec2.getOrDefault(word, 0);
            dotProduct += count1 * count2;
            norm1 += count1 * count1;
            norm2 += count2 * count2;
        }

        // 避免除以0（空文本情况）
        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    // 将结果写入文件（保留两位小数）
    static void writeResult(String filePath, double similarity) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(String.format("%.2f", similarity));
        }
    }
}
