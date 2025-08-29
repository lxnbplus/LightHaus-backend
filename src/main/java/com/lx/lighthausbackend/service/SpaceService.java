package com.lx.lighthausbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lx.lighthausbackend.model.dto.space.SpaceAddRequest;
import com.lx.lighthausbackend.model.dto.space.SpaceQueryRequest;
import com.lx.lighthausbackend.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lx.lighthausbackend.model.entity.User;
import com.lx.lighthausbackend.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author chandler
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2025-08-29 10:21:23
*/
public interface SpaceService extends IService<Space> {
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    void validSpace(Space space, boolean add);

    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 填充空间信息
     * @param space
     */
    void fillSpaceBySpaceLevel(Space space);

    void checkSpaceAuth(User loginUser, Space space);
}
