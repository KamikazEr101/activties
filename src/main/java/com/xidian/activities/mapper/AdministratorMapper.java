package com.xidian.activities.mapper;

import com.xidian.activities.entity.Administrator;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 管理员Mapper接口
 *
 * @author
 * @since
 */
@Mapper
public interface AdministratorMapper {

    /**
     * 根据用户名查询管理员
     *
     * @param username 用户名
     * @return 管理员信息
     */
    Administrator selectByUsername(@Param("username") String username);

    /**
     * 根据ID查询管理员
     *
     * @param id 管理员ID
     * @return 管理员信息
     */
    Administrator selectById(@Param("id") Long id);

    /**
     * 更新管理员信息
     *
     * @param administrator 管理员信息
     * @return 影响行数
     */
    int updateById(Administrator administrator);
}