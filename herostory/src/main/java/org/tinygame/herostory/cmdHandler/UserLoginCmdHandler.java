package org.tinygame.herostory.cmdHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.async.AsyncOperationProcessor;
import org.tinygame.herostory.login.LoginService;
import org.tinygame.herostory.login.db.UserEntity;
import org.tinygame.herostory.model.User;
import org.tinygame.herostory.model.UserManager;
import org.tinygame.herostory.msg.GameMsgProtocol;

/**
 * 用户登录指令处理器
 */
public class UserLoginCmdHandler implements ICmdHandler<GameMsgProtocol.UserLoginCmd> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserAttkCmdHandler.class);

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsgProtocol.UserLoginCmd cmd) {
        if (ctx == null || cmd == null) {
            return;
        }

        String userName = cmd.getUserName();
        String password = cmd.getPassword();

        LOGGER.info("用户登陆, userName = {}, password = {}", cmd.getUserName(), cmd.getPassword());

        AsyncOperationProcessor.getInstance().process(() -> {
            // 用户登录
            LoginService.getInstance().userLogin(userName, password,
                    (userEntity -> {

                        if (userEntity == null) {
                            LOGGER.error("用户登陆失败, userName = {}", cmd.getUserName());
                            return null;
                        }

                        LOGGER.info("当前线程 = {}",Thread.currentThread().getName());

                        LOGGER.info("用户登录成功，userId = {}，userName = {}",userEntity.userId,userEntity.userName);

                        //将用户加入字典，在场角色加入
                        User newUser = new User();
                        newUser.userId = userEntity.userId;
                        newUser.userName = userEntity.userName;
                        newUser.heroAvatar = userEntity.heroAvatar;
                        newUser.currHp = 100;
                        // 将用户加入管理器
                        UserManager.addUser(newUser);

                        // 将用户 Id 附着到 Channel
                        ctx.channel().attr(AttributeKey.valueOf("userId")).set(newUser.userId);

                        GameMsgProtocol.UserLoginResult.Builder resultBuilder = GameMsgProtocol.UserLoginResult.newBuilder();
                        resultBuilder.setUserId(newUser.userId);
                        resultBuilder.setUserName(newUser.userName);
                        resultBuilder.setHeroAvatar(newUser.heroAvatar);

                        // 构建结构并发送
                        GameMsgProtocol.UserLoginResult newResult = resultBuilder.build();
                        ctx.writeAndFlush(newResult);
                        return null;
                    }));


        });
    }
}
