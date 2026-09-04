package com.visupicture.api.aliyunai.model;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建万相通用图像编辑任务请求（wanx2.1-imageedit，扩图使用 function = expand）
 */
@Data
public class CreateImageEditTaskRequest implements Serializable {

    /**
     * 模型，固定为 "wanx2.1-imageedit"
     */
    private String model = "wanx2.1-imageedit";

    /**
     * 输入图像信息
     */
    private Input input;

    /**
     * 图像处理参数
     */
    private Parameters parameters;

    @Data
    public static class Input {
        /**
         * 必选，图像编辑功能，扩图固定为 "expand"
         */
        private String function = "expand";

        /**
         * 可选，提示词，用于引导扩图内容
         */
        private String prompt;

        /**
         * 必选，输入图像 URL
         */
        @Alias("base_image_url")
        private String baseImageUrl;
    }

    @Data
    public static class Parameters implements Serializable {
        /**
         * 可选，向上扩展比例，默认值 1.0，范围 [1.0, 2.0]
         */
        @Alias("top_scale")
        private Float topScale;

        /**
         * 可选，向下扩展比例，默认值 1.0，范围 [1.0, 2.0]
         */
        @Alias("bottom_scale")
        private Float bottomScale;

        /**
         * 可选，向左扩展比例，默认值 1.0，范围 [1.0, 2.0]
         */
        @Alias("left_scale")
        private Float leftScale;

        /**
         * 可选，向右扩展比例，默认值 1.0，范围 [1.0, 2.0]
         */
        @Alias("right_scale")
        private Float rightScale;

        /**
         * 可选，生成图片数量，范围 [1, 4]，默认值 1
         */
        private Integer n = 1;

        private static final long serialVersionUID = 1L;
    }

    private static final long serialVersionUID = 1L;
}
