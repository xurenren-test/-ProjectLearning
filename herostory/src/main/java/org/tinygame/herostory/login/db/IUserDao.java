package org.tinygame.herostory.login.db;

import org.apache.ibatis.annotations.Param;

public interface IUserDao {
    /**
     * 根据用户名获取用户
     * @param userName
     * @return
     */
    UserEntity getUserByName(@Param("userName") String userName);

    /**
     * 添加用户实体
     * @param newUserENtity
     */
    void insertInto(UserEntity newUserENtity);
}
