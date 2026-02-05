package com.mofadanqing.service.impl;

import com.mofadanqing.entity.AiTask;
import com.mofadanqing.mapper.AiTaskMapper;
import com.mofadanqing.service.BomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class AiTaskExecutor {

    @Autowired
    private AiTaskMapper aiTaskMapper;

    @Autowired
    private BomService bomService;

    @Async
    public void executeTask(String taskId) {
        try {
            AiTask task = aiTaskMapper.selectById(taskId);
            if (task == null) return;

            String userPrompt = task.getPrompt();
            if (!StringUtils.hasText(userPrompt)) {
                userPrompt = "embroidery art";
            }

            // --- Layer 1: Skeleton/Hair Generation ---
            String layer1Prompt = userPrompt + ", black and white line art, intricate hair texture, embroidery style, ink painting style, 0.07mm lines, no color, minimalist, sketch, high contrast, monochrome";
            String layer1Url = callDashScopeImage(layer1Prompt, task.getRefImg());
            
            // --- Layer 2: Polishing/Silk Generation ---
            String layer2Prompt = userPrompt + ", colorful silk embroidery, glossy silk texture, vibrant colors, close up, detailed texture, no background, embroidery fill, specular reflection, satin finish";
            String layer2Url = callDashScopeImage(layer2Prompt, task.getRefImg());

            // Update Task
            List<String> images = Arrays.asList(layer1Url, layer2Url);
            task.setImages(images);
            
            // --- BOM Calculation ---
            Map<String, BigDecimal> bomData = bomService.calculateBom(layer2Url);
            
            // Store BOM data in the `errorMessage` field as a JSON string for persistence
            String bomJson = String.format("{\"hair\":%s,\"silk\":%s,\"cost\":%s}", 
                bomData.get("hairLength"), bomData.get("silkWeight"), bomData.get("estimatedCost"));
            task.setErrorMessage(bomJson);

            task.setStatus("COMPLETED");
            task.setUpdateTime(LocalDateTime.now());
            aiTaskMapper.updateById(task);

        } catch (Exception e) {
            e.printStackTrace();
            // Handle Failure
            AiTask task = aiTaskMapper.selectById(taskId);
            if (task != null) {
                task.setStatus("FAILED");
                task.setErrorMessage(e.getMessage());
                task.setUpdateTime(LocalDateTime.now());
                aiTaskMapper.updateById(task);
            }
        }
    }

    private String callDashScopeImage(String prompt, String refImg) throws Exception {
        String apiKey = System.getenv("DASHSCOPE_API_KEY");
        // Check for API Key
        if (apiKey == null) {
            System.err.println("WARN: DASHSCOPE_API_KEY not found in environment. Please set it.");
             return "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=" + java.net.URLEncoder.encode(prompt, "UTF-8") + "&image_size=square";
        }

        try {
            // Use ProcessBuilder to call Python script
            String scriptPath = "backend/scripts/generate_image.py";
            // Check if file exists, if not try absolute path or relative to user.dir
            java.io.File scriptFile = new java.io.File(scriptPath);
            if (!scriptFile.exists()) {
                scriptPath = "D:/file/code/tiaozhanbei/backend/scripts/generate_image.py";
            }
            
            // Double check existence to avoid confusion
            if (!new java.io.File(scriptPath).exists()) {
                 System.err.println("Script not found at: " + scriptPath + ". Using current dir: " + System.getProperty("user.dir"));
                 return "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=" + java.net.URLEncoder.encode(prompt, "UTF-8") + "&image_size=square";
            }

            // Explicitly pass model parameter to ensure correct model usage
            ProcessBuilder pb = new ProcessBuilder("python", scriptPath, "--prompt", prompt, "--model", "z-image-turbo");
            pb.environment().put("DASHSCOPE_API_KEY", apiKey); // Pass env explicitly
            pb.redirectErrorStream(true); // Merge stderr to stdout to prevent deadlock
            
            // Log command for debugging
            System.out.println("Executing Python Command: " + String.join(" ", pb.command()));
            
            Process process = pb.start();
            
            // Read Output (Merged)
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                // Print the full output for debugging
                String errorMsg = "Python Script Failed (Exit Code " + exitCode + "). Output:\n" + output.toString();
                System.err.println(errorMsg);
                throw new RuntimeException(errorMsg);
            }
            
            String outputStr = output.toString();
            String startMarker = "###URL_START###";
            String endMarker = "###URL_END###";
            
            int startIndex = outputStr.indexOf(startMarker);
            int endIndex = outputStr.indexOf(endMarker);
            
            if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                return outputStr.substring(startIndex + startMarker.length(), endIndex).trim();
            }
            
            throw new RuntimeException("Invalid output format from Python script. Raw Output: " + outputStr);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Python execution failed, falling back to mock: " + e.getMessage());
            // Re-throw to propagate failure to task status
            throw e; 
        }
    }
}
