package com.visupicture.api.imagesearch.sub;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 百度识图请求公共工具：模拟浏览器请求头 + 携带百度 Cookie 以通过风控
 */
@Slf4j
public class BaiduHttpHelper {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36";

    private static volatile String cachedCookie;

    private BaiduHttpHelper() {
    }

    /**
     * 获取百度 Cookie（BAIDUID 等），带本地缓存
     */
    public static String getBaiduCookie() {
        if (cachedCookie == null) {
            synchronized (BaiduHttpHelper.class) {
                if (cachedCookie == null) {
                    cachedCookie = fetchCookie();
                }
            }
        }
        return cachedCookie;
    }

    public static String getUserAgent() {
        return USER_AGENT;
    }

    private static String fetchCookie() {
        try (HttpResponse resp = HttpUtil.createGet("https://www.baidu.com/")
                .header("User-Agent", USER_AGENT)
                .timeout(5000)
                .execute()) {
            List<java.net.HttpCookie> cookies = resp.getCookies();
            if (CollUtil.isNotEmpty(cookies)) {
                return cookies.stream()
                        .map(c -> c.getName() + "=" + c.getValue())
                        .collect(Collectors.joining("; "));
            }
        } catch (Exception e) {
            log.warn("获取百度 Cookie 失败", e);
        }
        return "";
    }
}
