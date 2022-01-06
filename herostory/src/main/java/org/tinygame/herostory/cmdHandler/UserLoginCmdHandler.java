package org.tinygame.herostory.cmdHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        LOGGER.info("用户登陆, userName = {}, password = {}", cmd.getUserName(), cmd.getPassword());

        UserEntity userEntity = null;
        try {
            userEntity = LoginService.getInstance().userLogin(cmd.getUserName(), cmd.getPassword());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        if (userEntity == null) {
            LOGGER.error("用户登陆失败, userName = {}", cmd.getUserName());
            return;
        }

        int userId = userEntity.userId;
        String userName = userEntity.userName;
        String heroAvatar =  userEntity.heroAvatar;

        //将用户加入字典，在场角色加入
        User newUser = new User();
        newUser.userId = userId;
        newUser.userName = userName;
        newUser.heroAvatar = heroAvatar;
        newUser.currHp = 100;
        // 将用户加入管理器
        UserManager.addUser(newUser);

        // 将用户 Id 附着到 Channel
        ctx.channel().attr(AttributeKey.valueOf("userId")).set(userId);

        GameMsgProtocol.UserLoginResult.Builder resultBuilder = GameMsgProtocol.UserLoginResult.newBuilder();
        resultBuilder.setUserId(newUser.userId);
        resultBuilder.setUserName(newUser.userName);
        resultBuilder.setHeroAvatar(newUser.heroAvatar);

        // 构建结构并发送
        GameMsgProtocol.UserLoginResult newResult = resultBuilder.build();
        ctx.writeAndFlush(newResult);
    }
}
