package com.visupicture.api.imagesearch.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 图片搜索结果
 */
@Data
@AllArgsConstructor
public class ImageSearchResult {

    /**
     * 缩略图地址
     */
    private String thumbUrl;

    /**
     * 来源地址
     */
    private String fromUrl;
}