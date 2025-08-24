package com.lx.lighthausbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lx.lighthausbackend.model.dto.user.UserQueryRequest;
import com.lx.lighthausbackend.model.entity.User;
import com.lx.lighthausbackend.model.vo.LoginUserVO;
import com.lx.lighthausbackend.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author chandler
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2025-08-21 22:32:04
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户(仅用于业务代码内部，返回值为全部信息的User)
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    // 密码加密
    String getEncryptPassword(String userPassword);

    // user -> LoginUserVO
    LoginUserVO getLoginUserVO(User user);

    // user -> UserVO
    UserVO getUserVO(User user);

    // list<user> -> list<UserVO>
    List<UserVO> getUserVOList(List<User> userList);

    // 查询条件
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);

}
