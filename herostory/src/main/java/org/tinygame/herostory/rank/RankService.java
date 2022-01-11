package org.tinygame.herostory.rank;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.async.AsyncOperationProcessor;
import org.tinygame.herostory.async.IAsyncOperation;
import org.tinygame.herostory.login.LoginService;
import org.tinygame.herostory.util.RedisUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * 排行榜服务
 */
public final class RankService {

    static private final Logger LOGGER = LoggerFactory.getLogger(RankService.class);
    // 单例对象
    private static final RankService instance = new RankService();

    private RankService() {
    }

    /**
     * 获取单例对象
     *
     * @return 排行榜服务
     */
    public static RankService getInstance() {
        return instance;
    }

    /**
     * 获取排名次数
     * 获取数据是访问redis的，直接返回 list 会造成大量io
     *
     * @param callback 回调函数
     */
    public void getRank(Function<List<RankItem>, Void> callback) {
        if (callback == null) {
            return;
        }
        IAsyncOperation asyncOp = new AsyncGetRank(){
            @Override
            public void doFinish() {
                callback.apply(this.getRankItemList());
            }
        };

        AsyncOperationProcessor.getInstance().process(asyncOp);
    }

    /**
     * 异步方式获取排行榜
     */
    private class AsyncGetRank implements IAsyncOperation {
        // 排名列表
        private List<RankItem> rankItemList = null;

        /**
         * 获取排名条目列表
         *
         * @return 排名条目列表
         */
        public List<RankItem> getRankItemList() {
            return rankItemList;
        }

        @Override
        public void doAsunc() {

            try (Jedis redis = RedisUtil.getRedis()) {
                // 获取排名前十的字符串集合
                // Tuple 元组，与列表list作用一样，不同的是可以存储不同的数据类型，比如同时存储int、string、list等
                Set<Tuple> valSet = redis.zrevrangeWithScores("Rank", 0, 9);

                rankItemList = new ArrayList<RankItem>();

                int rankId = 0;

                for (Tuple t : valSet) {
                    // 获取用户id。（获取redis中的key）
                    int userId = Integer.parseInt(t.getElement());
                    // 获取用户的基本信息
                    String jsonStr = redis.hget("User_" + userId, "BasicInfo");
                    if (jsonStr == null || jsonStr.isEmpty()) {
                        continue;
                    }
                    // 解析json
                    JSONObject jsonObj = JSONObject.parseObject(jsonStr);

                    RankItem newItem = new RankItem();
                    newItem.rankId = ++rankId;
                    newItem.userId = userId;
                    newItem.userName = jsonObj.getString("userName");
                    newItem.heroAvatar = jsonObj.getString("heroAvatar");
                    newItem.win = (int) t.getScore();

                    rankItemList.add(newItem);
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

}
