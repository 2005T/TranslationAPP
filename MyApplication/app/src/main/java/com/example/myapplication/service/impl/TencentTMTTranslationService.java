package com.example.myapplication.service.impl;

import android.util.Base64;
import android.util.Log;

import com.example.myapplication.model.TranslationResult;
import com.example.myapplication.service.TranslationService;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 腾讯机器翻译服务（TMT）实现
 */
public class TencentTMTTranslationService implements TranslationService {
    
    private static final String TAG = "TencentTMTTranslation";
    private static final String SECRET_ID = "AKTID2xfARWNAsWCj8Zr7D9tRs7B1hDXdWDkB";
    private static final String SECRET_KEY = "LUSEnAJKFIMn24ufFftw77vVnuphQvHS";
    private static final String SERVICE = "tmt";
    private static final String HOST = "tmt.tencentcloudapi.com";
    private static final String REGION = "ap-guangzhou";  // 使用广州地区的服务
    private static final String ACTION = "TextTranslate";
    private static final String VERSION = "2018-03-21";
    private static final String ALGORITHM = "TC3-HMAC-SHA256";
    
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    
    @Override
    public void translateText(String sourceText, String targetLanguage, String sourceLanguage, 
                              TranslationCallback callback) {
        try {
            // 准备请求参数
            String requestBody = buildRequestBody(sourceText, sourceLanguage, targetLanguage);
            
            // 计算签名
            String authorization = calculateAuthorization(requestBody);
            
            // 构建HTTP请求
            Request request = buildRequest(requestBody, authorization);
            
            // 发送请求
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Translation request failed: " + e.getMessage());
                    callback.onError("翻译请求失败: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        handleSuccessResponse(responseBody, sourceText, sourceLanguage, targetLanguage, callback);
                    } else {
                        Log.e(TAG, "Translation error: " + response.code() + " " + response.message());
                        callback.onError("翻译失败: " + response.code() + " " + response.message());
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception in translation service: " + e.getMessage());
            callback.onError("翻译服务异常: " + e.getMessage());
        }
    }
    
    private String buildRequestBody(String sourceText, String sourceLanguage, String targetLanguage) {
        // 转换为腾讯云支持的语言代码格式
        String tencentSourceLang = convertToTencentLanguageCode(sourceLanguage);
        String tencentTargetLang = convertToTencentLanguageCode(targetLanguage);
        
        TranslationRequest requestData = new TranslationRequest();
        requestData.sourceText = sourceText;
        requestData.source = tencentSourceLang;
        requestData.target = tencentTargetLang;
        requestData.projectId = 0; // 默认项目ID
        
        return gson.toJson(requestData);
    }
    
    private Request buildRequest(String requestBody, String authorization) {
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        
        return new Request.Builder()
                .url("https://" + HOST)
                .post(RequestBody.create(MediaType.parse("application/json"), requestBody))
                .addHeader("Authorization", authorization)
                .addHeader("Content-Type", "application/json")
                .addHeader("Host", HOST)
                .addHeader("X-TC-Action", ACTION)
                .addHeader("X-TC-Timestamp", timestamp)
                .addHeader("X-TC-Version", VERSION)
                .addHeader("X-TC-Region", REGION)
                .build();
    }
    
    private String calculateAuthorization(String requestBody) {
        try {
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String date = sdf.format(new Date(Long.parseLong(timestamp) * 1000));
            
            // ************* 步骤 1：拼接规范请求串 *************
            String httpRequestMethod = "POST";
            String canonicalUri = "/";
            String canonicalQueryString = "";
            String canonicalHeaders = "content-type:application/json\nhost:" + HOST + "\n";
            String signedHeaders = "content-type;host";
            
            String hashedRequestPayload = sha256Hex(requestBody);
            String canonicalRequest = httpRequestMethod + "\n" 
                    + canonicalUri + "\n" 
                    + canonicalQueryString + "\n"
                    + canonicalHeaders + "\n" 
                    + signedHeaders + "\n" 
                    + hashedRequestPayload;
            
            // ************* 步骤 2：拼接待签名字符串 *************
            String credentialScope = date + "/" + SERVICE + "/tc3_request";
            String hashedCanonicalRequest = sha256Hex(canonicalRequest);
            String stringToSign = ALGORITHM + "\n" 
                    + timestamp + "\n" 
                    + credentialScope + "\n" 
                    + hashedCanonicalRequest;
            
            // ************* 步骤 3：计算签名 *************
            byte[] secretDate = hmacSha256(("TC3" + SECRET_KEY).getBytes(StandardCharsets.UTF_8), date);
            byte[] secretService = hmacSha256(secretDate, SERVICE);
            byte[] secretSigning = hmacSha256(secretService, "tc3_request");
            String signature = bytesToHex(hmacSha256(secretSigning, stringToSign));
            
            // ************* 步骤 4：拼接 Authorization *************
            return ALGORITHM + " "
                    + "Credential=" + SECRET_ID + "/" + credentialScope + ", "
                    + "SignedHeaders=" + signedHeaders + ", "
                    + "Signature=" + signature;
            
        } catch (Exception e) {
            Log.e(TAG, "Error calculating authorization: " + e.getMessage());
            return "";
        }
    }
    
    private void handleSuccessResponse(String responseBody, String sourceText, String sourceLanguage, 
                                      String targetLanguage, TranslationCallback callback) {
        try {
            TranslationResponse response = gson.fromJson(responseBody, TranslationResponse.class);
            
            if (response != null && response.response != null && response.response.targetText != null) {
                TranslationResult result = new TranslationResult(
                        sourceText,
                        response.response.targetText,
                        sourceLanguage,
                        targetLanguage,
                        TranslationResult.TranslationType.TEXT
                );
                callback.onSuccess(result);
            } else {
                callback.onError("翻译结果解析失败");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing translation result: " + e.getMessage());
            callback.onError("翻译结果解析失败: " + e.getMessage());
        }
    }
    
    // 语言代码转换（将标准语言代码转换为腾讯云需要的格式）
    private String convertToTencentLanguageCode(String languageCode) {
        // 根据需要转换语言代码，例如 "zh-CN" -> "zh", "en-US" -> "en"
        if (languageCode.equals("auto")) {
            return "auto";
        }
        
        // 简单处理，取前两个字符作为语言代码
        if (languageCode.length() >= 2) {
            return languageCode.substring(0, 2).toLowerCase();
        }
        
        return languageCode;
    }
    
    // SHA256 哈希
    private String sha256Hex(String s) throws NoSuchAlgorithmException {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
        byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(d);
    }
    
    // HMAC-SHA256
    private byte[] hmacSha256(byte[] key, String msg) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, mac.getAlgorithm());
        mac.init(secretKeySpec);
        return mac.doFinal(msg.getBytes(StandardCharsets.UTF_8));
    }
    
    // 字节数组转十六进制字符串
    private String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02x", b & 0xff));
        }
        return builder.toString();
    }
    
    // 请求对象
    private static class TranslationRequest {
        @SerializedName("SourceText")
        String sourceText;
        
        @SerializedName("Source")
        String source;
        
        @SerializedName("Target")
        String target;
        
        @SerializedName("ProjectId")
        int projectId;
    }
    
    // 响应对象
    private static class TranslationResponse {
        @SerializedName("Response")
        ResponseData response;
        
        static class ResponseData {
            @SerializedName("RequestId")
            String requestId;
            
            @SerializedName("TargetText")
            String targetText;
            
            @SerializedName("Source")
            String source;
            
            @SerializedName("Target")
            String target;
        }
    }
} 