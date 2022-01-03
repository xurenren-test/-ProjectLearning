package org.tinygame.herostory.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理器
 */
public final class UserManager {
    // 私有化构造器
    private UserManager() {
    }

    //用户字典，根据用户id与用户建立一对一的关系
    static private final Map<Integer, User> userMap = new HashMap<>();

    /**
     * 添加用户
     */
    public static void addUser(User newUser) {
        if (newUser != null) {
            userMap.put(newUser.userId, newUser);
        }
    }

    /**
     * 根据id移除用户
     */
    public static void removeUserById(int userId) {
        userMap.remove(userId);
    }

    /**
     * 用户列表
     */
    public static Collection<User> listUser() {
        return userMap.values();
    }

    /**
     * 根据id获取用户
     * @param userId 用户id
     * @return 返回用户对象
     */
    public static User getUserById(int userId) {
        return userMap.get(userId);
    }

}
