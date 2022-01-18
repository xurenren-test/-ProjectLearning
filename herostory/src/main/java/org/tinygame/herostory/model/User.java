package org.tinygame.herostory.model;

/**
 * 用户
 */
public class User {
    /**
     * 用户id
     */
    public int userId;

    /**
     * 用户名
     */
    public String userName;

    /**
     * 英雄形象
     */
    public String heroAvatar;

    /**
     * 移动状态
     */
    public final MoveState moveState = new MoveState();

    /**
     * 当前血量
     */
    public int currHp;

    /**
     * 是否已经死亡
     */
    public boolean died;

}
