package com.lx.lighthausbackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import com.lx.lighthausbackend.config.CosClientConfig;
import com.lx.lighthausbackend.exception.BusinessException;
import com.lx.lighthausbackend.exception.ErrorCode;
import com.lx.lighthausbackend.manager.CosManager;
import com.lx.lighthausbackend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;

/**
 * 图片上传模版（抽象类）
 */
@Slf4j
public abstract class PictureUploadTemplate {
  
    @Resource
    protected CosManager cosManager;
  
    @Resource  
    protected CosClientConfig cosClientConfig;
  
    /**  
     * 模板方法，定义上传流程  
     */
    // com.lx.lighthausbackend.manager.upload.PictureUploadTemplate

    public final UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        // 1. 校验输入
        validPicture(inputSource);

        // 2. 生成文件名/后缀（保留兜底 png 策略）
        String uuid = cn.hutool.core.util.RandomUtil.randomString(16);
        String originFilename = getOriginFilename(inputSource);
        String suffix = cn.hutool.core.io.FileUtil.getSuffix(originFilename);
        log.info("[TEMPLATE] originFilename={}, suffix(before)={}", originFilename, suffix);
        if (cn.hutool.core.util.StrUtil.isBlank(suffix)
                || !cn.hutool.core.collection.CollUtil.contains(
                java.util.Arrays.asList("jpg", "jpeg", "png", "webp"),
                suffix.toLowerCase())) {
            suffix = "png";
        }

        // 原图 key（CosManager 内会生成 webp + thumbnail，并在成功后删除原图）
        String baseName  = cn.hutool.core.date.DateUtil.formatDate(new java.util.Date()) + "_" + uuid;
        String uploadKey = String.format("/%s/%s.%s", uploadPathPrefix, baseName, suffix);
        log.info("[TEMPLATE] impl={}, rawKey={}", this.getClass().getName(), uploadKey);

        File file = null;
        try {
            // 3. 创建临时文件 & 写入（本地或 URL）
            file = File.createTempFile(baseName, null);
            processFile(inputSource, file);

            // 4. 上传到 COS（CosManager：生成 webp + thumbnail，并确认后删除原图）
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadKey, file);

            // 5. 解析处理结果（严格按你示例的写法）
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            if (cn.hutool.core.collection.CollUtil.isNotEmpty(objectList)) {
                CIObject compressedCiObject = objectList.get(0);   // webp
                CIObject thumbnailCiObject  = objectList.size() > 1 ? objectList.get(1) : null; // 缩略图

                // 获取imageInfo对象
                ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
                UploadPictureResult result = buildResult(originFilename, compressedCiObject, thumbnailCiObject, imageInfo);

                // 回填 originUrl（即使原图被删除，也仅作为来源记录）
                result.setOriginUrl(cosClientConfig.getHost() + "/" + uploadKey);
                return result;
            }

            // 6. 如果没有返回处理结果，兜底：用原图信息封装
            ImageInfo imageInfo = null;
            if (putObjectResult.getCiUploadResult() != null
                    && putObjectResult.getCiUploadResult().getOriginalInfo() != null) {
                imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            }
            UploadPictureResult fallback = buildResult(originFilename, file, uploadKey, imageInfo);
            fallback.setOriginUrl(cosClientConfig.getHost() + "/" + uploadKey);
            return fallback;

        } catch (Exception e) {
            log.error("图片上传到对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            // 7. 清理临时文件
            deleteTempFile(file);
        }
    }
  
    /**  
     * 校验输入源（本地文件或 URL）  
     */  
    protected abstract void validPicture(Object inputSource);  
  
    /**  
     * 获取输入源的原始文件名  
     */  
    protected abstract String getOriginFilename(Object inputSource);  
  
    /**  
     * 处理输入源并生成本地临时文件  
     */  
    protected abstract void processFile(Object inputSource, File file) throws Exception;

    /**
     * 封装返回结果
     */
    private UploadPictureResult buildResult(String originFilename,
                                            CIObject compressedCiObject,
                                            CIObject thumbnailCiObject,ImageInfo imageInfo) {
        UploadPictureResult result = new UploadPictureResult();
        result.setPicName(FileUtil.mainName(originFilename));
        result.setPicColor(imageInfo.getAve());


        // 压缩图(webp)地址
        result.setUrl(cosClientConfig.getHost() + "/" + compressedCiObject.getKey());

        // 原图地址（注意：可能已经删除，但数据库里保留）
        result.setOriginUrl(cosClientConfig.getHost() + "/" + FileUtil.mainName(originFilename));

        // 缩略图地址（可选字段，你可以加到 DTO / VO）
        if (thumbnailCiObject != null) {
            result.setThumbnailUrl(cosClientConfig.getHost() + "/" + thumbnailCiObject.getKey());
        }

        return result;
    }


    /**  
     * 封装返回结果  
     */  
    private UploadPictureResult buildResult(String originFilename, File file, String uploadPath, ImageInfo imageInfo) {  
        UploadPictureResult uploadPictureResult = new UploadPictureResult();  
        int picWidth = imageInfo.getWidth();  
        int picHeight = imageInfo.getHeight();  
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        uploadPictureResult.setPicName(FileUtil.mainName(originFilename));  
        uploadPictureResult.setPicWidth(picWidth);  
        uploadPictureResult.setPicHeight(picHeight);  
        uploadPictureResult.setPicScale(picScale);  
        uploadPictureResult.setPicFormat(imageInfo.getFormat());
        uploadPictureResult.setPicColor(imageInfo.getAve());
        uploadPictureResult.setPicSize(FileUtil.size(file));  
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);  
        return uploadPictureResult;  
    }  
  
    /**  
     * 删除临时文件  
     */  
    public void deleteTempFile(File file) {  
        if (file == null) {  
            return;  
        }  
        boolean deleteResult = file.delete();  
        if (!deleteResult) {  
            log.error("file delete error, filepath = {}", file.getAbsolutePath());  
        }  
    }  
}
