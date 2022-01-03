package org.tinygame.herostory.model;

/**
 *用户
 */
public class User {
    /**
     *用户id
      */
    public int userId;

    /**
     *英雄形象
     */
    public String heroAvatar;

    /**
     * 移动状态
     */
    public final MoveState moveState = new MoveState();
}
