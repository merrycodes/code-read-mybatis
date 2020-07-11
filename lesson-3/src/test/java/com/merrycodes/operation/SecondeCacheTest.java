package com.merrycodes.operation;

import com.merrycodes.mapper.UserMapper;
import com.merrycodes.model.User;
import com.merrycodes.utils.MockUtils;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.decorators.*;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @author MerryCodes
 * @date 2020/6/14 23:47
 */
public class SecondeCacheTest {

    private SqlSessionFactory factory;

    private Configuration configuration;

    @Before
    public void init() throws IOException {
        SqlSessionFactoryBuilder factoryBuilder = new SqlSessionFactoryBuilder();
        factory = factoryBuilder.build(Resources.getResourceAsStream("mybatis-config.xml"));
        configuration = factory.getConfiguration();
    }

    /**
     * 调试中cache中的结构是装饰器+责任链
     *
     * @see SynchronizedCache SynchronizedCache 线程安全
     * @see LoggingCache LoggingCache 记录日志
     * @see SerializedCache SerializedCache 序列化
     * @see LruCache LruCache 溢出淘汰（最近最少使用）
     * @see PerpetualCache 存储操作
     */
    @Test
    public void cacheTest1() {
        Cache cache = configuration.getCache("com.merrycodes.mapper.UserMapper");
        User user = MockUtils.mockUser();
        // 设置缓存
        cache.putObject("merrycodes", user);
        cache.getObject("merrycodes");
    }

    /**
     * 缓存实现拓展<br>
     * {@link PerpetualCache} 默认的存储实现
     *
     * @see UserMapper 注解 @CacheNamespace 使用自定义存储逻辑
     */
    @Test
    public void cacheTest2() {
        Cache cache = configuration.getCache("com.merrycodes.mapper.UserMapper");
        User user = MockUtils.mockUser();
        // 设置缓存
        cache.putObject("merrycodes", user);
        System.out.println(cache.getObject("merrycodes"));
    }

    /**
     * 溢出淘汰<br>
     * {@link FifoCache} 底层维护队列 --> LinkedList<br>
     * {@link LruCache} 默认实现 底层维护 --> LinkedHashMap 使用 accessOrder 排序机制
     *
     * @see UserMapper 注解 @CacheNamespace eviction = FifoCache.class,size = 10
     */
    @Test
    public void cacheTest3() {
        Cache cache = configuration.getCache("com.merrycodes.mapper.UserMapper");
        User user = MockUtils.mockUser();
        for (int i = 0; i < 12; i++) {
            cache.putObject("merrycodes" + i, user);
        }
        System.out.println(cache.getObject("merrycodes"));
    }

    /**
     * 序列化
     *
     * @see UserMapper 注解 @CacheNamespace readWrite = false
     */
    @Test
    public void cacheTest4() {
        Cache cache = configuration.getCache("com.merrycodes.mapper.UserMapper");
        User user = MockUtils.mockUser();
        cache.putObject("merrycodes", user);
        // 线程一
        Object userOne = cache.getObject("merrycodes");
        // 线程二
        Object userTwo = cache.getObject("merrycodes");
        System.out.println(userOne == userTwo);
    }

    /**
     * 命中条件:
     * <ol>
     *     <li>SqlSession必须提交 / 设置了自动提交也不行，需要手动提交</li>
     *     <li>SQL和参数<strong>相同</strong></li>
     *     <li>MappedStatement<strong>相同</strong></li>
     * </ol>
     *
     * @see UserMapper useCache = false / flushCache = Options.FlushCachePolicy.TRUE
     */
    @Test
    public void cacheTest5() {
        // 查询一
        // 加了自动提交，还是要手动提交才能命中二级缓存
        SqlSession sqlSessionOne = factory.openSession();
        UserMapper mapperOne = sqlSessionOne.getMapper(UserMapper.class);
        User userOne = mapperOne.selectByIdOne(10);
        sqlSessionOne.commit();
        // 查询二
        SqlSession sqlSessionTwo = factory.openSession();
        UserMapper mapperTwo = sqlSessionTwo.getMapper(UserMapper.class);
        User userTwo = mapperTwo.selectByIdOne(10);
//        User userTwo = mapperTwo.selectByIdTwo(10);
    }

}