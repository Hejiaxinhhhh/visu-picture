package com.visupicture.model.dto.picture;

import com.visupicture.api.aliyunai.model.CreateOutPaintingTaskRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建扩图任务请求
 */
@Data
public class CreatePictureOutPaintingTaskRequest implements Serializable {

    /**
     * 图片 id
     */
    private Long pictureId;

    /**
     * 扩图模型：image-out-painting（默认，专用扩图）/ wanx2.1-imageedit（万相通用图像编辑）
     */
    private String model;

    /**
     * 可选，扩图提示词（wanx2.1-imageedit 模型使用）
     */
    private String prompt;

    /**
     * 可选，四方向统一扩展比例，范围 [1.0, 2.0]（wanx2.1-imageedit 模型使用）
     */
    private Float expandScale;

    /**
     * 扩图参数
     */
    private CreateOutPaintingTaskRequest.Parameters parameters;

    private static final long serialVersionUID = 1L;
}