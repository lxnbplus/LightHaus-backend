package com.lx.lighthausbackend.manager;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import com.lx.lighthausbackend.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    // ... 一些操作 COS 的方法

    /**
     * 上传对象
     *
     * @param key  唯一键
     * @param file 文件
     */
    public void putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        cosClient.putObject(putObjectRequest);
    }

    /**
     * 下载对象
     *
     * @param key 唯一键
     */
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }


    /**
     * 上传对象（附带图片信息）
     *
     * @param key  唯一键
     * @param file 文件
     */
    public PutObjectResult putPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);

        PicOperations picOperations = new PicOperations();
        picOperations.setIsPicInfo(1);

        List<PicOperations.Rule> rules = new ArrayList<>();

        // 1) webp 压缩图
        String webpKey = FileUtil.mainName(key) + ".webp";
        PicOperations.Rule compressRule = new PicOperations.Rule();
        compressRule.setRule("imageMogr2/format/webp");
        compressRule.setBucket(cosClientConfig.getBucket());
        compressRule.setFileId(webpKey);
        rules.add(compressRule);

        // 2) 缩略图（这里我建议也转成 webp，更省空间；如要保留原后缀，就去掉 format/webp）
        String thumbnailKey = FileUtil.mainName(key) + "_thumbnail.webp";
        PicOperations.Rule thumbnailRule = new PicOperations.Rule();
        thumbnailRule.setBucket(cosClientConfig.getBucket());
        thumbnailRule.setFileId(thumbnailKey);
        thumbnailRule.setRule("imageMogr2/thumbnail/256x256/format/webp");
        rules.add(thumbnailRule);

        picOperations.setRules(rules);
        putObjectRequest.setPicOperations(picOperations);

        PutObjectResult result = cosClient.putObject(putObjectRequest);

        // --- 删除原图（只有在至少 webp 成功时才删） ---
        try {
            ProcessResults pr = result.getCiUploadResult().getProcessResults();
            if (pr != null && CollUtil.isNotEmpty(pr.getObjectList())) {
                for (CIObject obj : pr.getObjectList()) {
                    if (obj.getKey().endsWith(".webp")) {
                        cosClient.deleteObject(cosClientConfig.getBucket(), key);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[COS] 删除原图失败: {}", key, e.getMessage());
        }

        return result;
    }

    /**
     * 删除对象
     *
     * @param key 文件 key
     */
    public void deleteObject(String key) throws CosClientException {
        cosClient.deleteObject(cosClientConfig.getBucket(), key);
    }


}
