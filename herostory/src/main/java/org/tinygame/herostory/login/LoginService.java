package org.tinygame.herostory.login;

import com.alibaba.fastjson.JSONObject;
import com.sun.scenario.effect.impl.prism.PrCropPeer;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.MySqlSessionFactory;
import org.tinygame.herostory.async.AsyncOperationProcessor;
import org.tinygame.herostory.async.IAsyncOperation;
import org.tinygame.herostory.login.db.IUserDao;
import org.tinygame.herostory.login.db.UserEntity;
import org.tinygame.herostory.util.RedisUtil;
import redis.clients.jedis.Jedis;

import java.util.function.Function;

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
     *
     * @param userName
     * @param password
     * @param callbak  异步回调函数
     */
    public void userLogin(String userName, String password, Function<UserEntity, Void> callbak) {
        if (userName == null || password == null) {
            return;
        }

        AsyncGetUserByName asyncOp = new AsyncGetUserByName(userName, password) {
            @Override
            public void doFinish() {
                if (callbak != null) {
                    // 执行回调函数
                    callbak.apply(this.getUserEntity());
                }
            }
        };
        // 执行异步操作
        AsyncOperationProcessor.getInstance().process(asyncOp);
    }

    /**
     * 更新redis中的用户基本信息
     * @param userEntity
     */
    private void updateUserBasicInfoInRedis(UserEntity userEntity){
        if (userEntity == null){
            return;
        }
        try (Jedis redis = RedisUtil.getRedis()){
            // 获取用户id
            int userId = userEntity.userId;
            // 转换为json
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("userId",userId);
            jsonObj.put("userName",userEntity.userName);
            jsonObj.put("heroAvatar",userEntity.heroAvatar);

            // 更新redis中数据
            redis.hset("User_" + userId,"BasicInfo",jsonObj.toJSONString());

        }catch (Exception e){
            LOGGER.error(e.getMessage(),e);
        }

    }

    /**
     * 异步方式获取用户
     */
    private class AsyncGetUserByName implements IAsyncOperation {

        private final String userName;
        private final String password;
        /**
         * 用户实体
         */
        private UserEntity _userEntity = null;

        /**
         * 参数构造器
         *
         * @param uName
         * @param pw
         */
        AsyncGetUserByName(String uName, String pw) {
            userName = uName;
            password = pw;
        }

        /**
         * 获取用户实体
         *
         * @return
         */
        public UserEntity getUserEntity() {
            return _userEntity;
        }

        @Override
        public int bindId() {
            // 也可以使用其他业务逻辑计算，例如hash算法
            return userName.charAt(userName.length() - 1);
        }

        @Override
        public void doAsunc() {
            // try（）这种用法指当逻辑块执行完成后，SqlSession自动调用close方法，无需手动调用
            try (SqlSession mySqlSession = MySqlSessionFactory.openSession()) {
                // 获取DAO (通过反射 + Javassist技术，根据IUserDao.xml生成)
                IUserDao dao = mySqlSession.getMapper(IUserDao.class);
                // 获取用户实体
                UserEntity UEntity = dao.getUserByName(userName);

                LOGGER.info("当前线程 = {}", Thread.currentThread().getName());

                if (UEntity != null) {
                    if (!password.equals(UEntity.password)) {
                        LOGGER.error("用户密码错误, userId = {}, userName = {}", UEntity.userId, userName);
                    }
                } else {
                    // 如果用户实体为空，则新建用户
                    UEntity = new UserEntity();
                    UEntity.userName = userName;
                    UEntity.password = password;
                    UEntity.heroAvatar = "Hero_Shaman"; // 默认

                    // 将用户实体添加到数据库
                    dao.insertInto(UEntity);
                }
                _userEntity = UEntity;

                LoginService.getInstance().updateUserBasicInfoInRedis(UEntity);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

}
