package org.tinygame.herostory.login;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.MySqlSessionFactory;
import org.tinygame.herostory.login.db.IUserDao;
import org.tinygame.herostory.login.db.UserEntity;

/**
 * 登录服务
 */
public final class LoginService {

    static private final Logger LOGGER = LoggerFactory.getLogger(LoginService.class);

    /**
     * 单例对象
     */
    private static final LoginService instance = new LoginService();

    private LoginService() {
    }

    /**
     * 获取单例对象
     *
     * @return
     */
    public static LoginService getInstance() {
        return instance;
    }

    /**
     * 用户登录
     * @param userName
     * @param password
     * @return
     */
    public UserEntity userLogin(String userName, String password) {
        if (userName == null || password == null) {
            return null;
        }

        // try（）这种用法指当逻辑块执行完成后，SqlSession自动调用close方法，无需手动调用
        try (SqlSession mySqlSession = MySqlSessionFactory.openSession()) {
            // 获取DAO (通过反射 + Javassist技术，根据IUserDao.xml生成)
            IUserDao dao = mySqlSession.getMapper(IUserDao.class);
            // 获取用户实体
            UserEntity userEntity = dao.getUserByName(userName);

            if (userEntity != null) {
                if (!password.equals(userEntity.password)) {
                    LOGGER.error("用户密码错误, userId = {}, userName = {}", userEntity.userId, userName);
                    throw new RuntimeException("用户密码错误");
                }
            } else {
                // 如果用户实体为空，则新建用户
                userEntity = new UserEntity();
                userEntity.userName = userName;
                userEntity.password = password;
                userEntity.heroAvatar = "Hero_Shaman"; // 默认

                // 将用户实体添加到数据库
                dao.insertInto(userEntity);
            }
            return userEntity;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }
}
