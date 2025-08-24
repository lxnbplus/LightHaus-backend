package com.lx.lighthausbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lx.lighthausbackend.model.dto.picture.PictureQueryRequest;
import com.lx.lighthausbackend.model.dto.picture.PictureUploadRequest;
import com.lx.lighthausbackend.model.entity.Picture;
import com.lx.lighthausbackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;
import com.lx.lighthausbackend.model.entity.User;

import javax.servlet.http.HttpServletRequest;

/**
* @author chandler
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-08-23 21:23:12
*/
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     *
     * @param multipartFile
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(MultipartFile multipartFile,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);

    /**
     * 获取查询条件
     *
     * @param pictureQueryRequest
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取图片封装类
     *
     * @param picture
     * @param request
     * @return
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 分页获取图片封装类
     *
     * @param picturePage
     * @param request
     * @return
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 校验图片
     *
     * @param picture
     */
    void validPicture(Picture picture);
}
