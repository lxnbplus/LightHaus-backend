package com.lx.lighthausbackend.api.imageSearch.sub;

import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.lx.lighthausbackend.exception.BusinessException;
import com.lx.lighthausbackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 获取以图搜图页面地址（step.1）
 * @Author: lx
 * @Date: 2023/9/27
 */
@Slf4j
public class GetImagePageUrlApi {

    /**
     * 获取图片页面地址
     *
     * @param imageUrl
     * @return
     */
    public static String getImagePageUrl(String imageUrl) {
        // 1. 准备请求参数
        Map<String, Object> formData = new HashMap<>();
        formData.put("image", imageUrl);
        formData.put("tn", "pc");
        formData.put("from", "pc");
        formData.put("image_source", "PC_UPLOAD_URL");
        // 获取当前时间戳
        long uptime = System.currentTimeMillis();
        // 请求地址
        String url = "https://graph.baidu.com/upload?uptime=" + uptime;
        String acsToken = "FwVBI04vLdr+CgHhYeGwKl0yjxBcfDRH+mYRRtn4sI1SwoJ7el6Hi2xF8XQgGtxCT7WqHHLHTy5V8G/ekB0GhASXoC3gK4RsSyvNMk0mKgN7K9Xf2Xt1UZ8LQv6W4NKov6jSAJu/ra7FcT4lcUAdr9vQKMgV0hsjWGI60NFjDTu8wakNUY/+KQWobLwzfbywVhY76ZBAY12WQM3w6XaAz+YWxnn/ynu1Q9tyseCo0QkedMQlprsBRpDuilc9KpevRGqsblKDwX9l1mIEF3abILU8/yIYYCIwdVc5rZ3jC7k8SieR8uZA+qnoy6hEgst+msVAI55QFvu01xaHz8NffMf+NdhmtspicxMrBS0GLgioNWEKammHE5pJClm4wSbiUvdM0FEVxGjoa/hQOJGPvuonp3t5PQWJlYOlbLnFWCgeS875X1Ti64Wra2Nq1FDt";

        try {
            // 2. 发送 POST 请求到百度接口
            String envAcsToken = System.getenv("BAIDU_ACS_TOKEN");
            String envCookie = System.getenv("BAIDU_COOKIE");
            String finalAcsToken = (envAcsToken != null && !envAcsToken.isEmpty()) ? envAcsToken : acsToken;

            HttpRequest request = HttpRequest.post(url)
                    .form(formData)
                    .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36")
                    .header("Origin", "https://graph.baidu.com")
                    .header("Referer", "https://graph.baidu.com/pcsearch")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .header("Acs-Token", finalAcsToken)
                    .timeout(10000);

            if (envCookie != null && !envCookie.isEmpty()) {
                request.header("Cookie", envCookie);
            }

            HttpResponse response = request.execute();
            // 判断响应状态
            if (HttpStatus.HTTP_OK != response.getStatus()) {
                log.error("HTTP请求失败，status={}", response.getStatus());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
            }
            // 解析响应
            String responseBody = response.body();
            Map<String, Object> result = JSONUtil.toBean(responseBody, Map.class);

            // 3. 处理响应结果
            if (result == null) {
                log.error("响应解析失败，body={}", responseBody);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
            }
            Object statusObj = result.get("status");
            int statusValue;
            try {
                statusValue = (statusObj instanceof Number) ? ((Number) statusObj).intValue() : Integer.parseInt(String.valueOf(statusObj));
            } catch (Exception parseEx) {
                log.error("status字段解析异常，statusObj={}, body={}", statusObj, responseBody);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
            }
            if (statusValue != 0) {
                log.error("业务失败，status={}，body={}", statusValue, responseBody);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
            }
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data == null) {
                log.error("响应缺少data字段，body={}", responseBody);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
            }
            String rawUrl = (String) data.get("url");
            // 对 URL 进行解码
            String searchResultUrl = URLUtil.decode(rawUrl, StandardCharsets.UTF_8);
            // 如果 URL 为空
            if (searchResultUrl == null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "未返回有效结果");
            }
            return searchResultUrl;
        } catch (Exception e) {
            log.error("搜索失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "搜索失败");
        }
    }

    public static void main(String[] args) {
        // 测试以图搜图功能
        String imageUrl = "https://lighthaus-1360289802.cos.ap-nanjing.myqcloud.com/public/1958755116771938306/2025-08-24_SuLxjfDQFACSx6P6.png";
        String result = getImagePageUrl(imageUrl);
        System.out.println("搜索成功，结果 URL：" + result);
    }
}
