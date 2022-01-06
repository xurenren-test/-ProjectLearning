package org.tinygame.herostory;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;


/**
 * sql 会话工厂
 */
public final class MySqlSessionFactory {

    private static SqlSessionFactory sqlSessionFactory;

    private MySqlSessionFactory() {
    }

    /**
     * 初始化
     */
    public static void init() {
        try {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("MyBatisConfig.xml"));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 开启sql会话
     * @return
     */
    public static SqlSession openSession(){
        if (sqlSessionFactory == null){
            throw new RuntimeException("sqlSessionFactory 尚未初始化");
        }
        return sqlSessionFactory.openSession(true);
    }
}
