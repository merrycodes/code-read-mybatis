package com.merrycodes.operation;

import com.merrycodes.mapper.UserMapper;
import com.merrycodes.model.User;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.io.IOException;

import static org.junit.Assert.assertSame;

/**
 * @author MerryCodes
 * @date 2020/6/8 13:08
 */
public class FirstCacheTest {

    private SqlSession sqlSession;

    @Before
    public void init() throws IOException {
        // 获取构建器
        SqlSessionFactoryBuilder factoryBuilder = new SqlSessionFactoryBuilder();
        // 解析XML 并构造会话工厂
        SqlSessionFactory sqlSessionFactory = factoryBuilder.build(Resources.getResourceAsStream("mybatis-config.xml"));
        sqlSession = sqlSessionFactory.openSession();
    }

    /**
     * <ol>
     *     <li>SQL和参数<strong>相同</strong></li>
     *     <li>MappedStatement<strong>相同</strong></li>
     *     <li>SqlSession<strong>相同</strong></li> 回话级缓存
     * </ol>
     */
    @Test
    public void test1() {
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        User userOne = userMapper.selectByIdOne(10);
//        User userTwo = userMapper.selectByIdOne(10); (userOne == userTwo) true

//        MappedStatement 不相同
//        User userTwo = userMapper.selectByIdTwo(10); (userOne == userTwo) false

//        SqlSession 不相同 （回话级缓存）
//        User userTwo = sqlSessionFactory.openSession().getMapper(UserMapper.class).selectByIdOne(10); (userOne == userTwo) false

//        MappedStatement 相同
//        User userTwo = sqlSession.selectOne("com.merrycodes.mapper.UserMapper.selectByIdOne",10); (userOne == userTwo) true

//        RowBounds 不一样（默认为RowBounds.DEFAULT）
        RowBounds rowBounds = new RowBounds(0, 10);
        User userTwo = (User) sqlSession.selectList("com.merrycodes.mapper.UserMapper.selectByIdOne", 10, rowBounds).get(0);
        assertSame(userOne, userTwo);
    }

    /**
     * <ol>
     *     <li>未手动清空缓存</li> {@link SqlSession#clearCache()} <strong>手动清空缓存</strong> <br>
     *     {@link SqlSession#rollback()} <strong>回滚</strong> <br> {@link SqlSession#commit()} <strong>提交</strong>
     *     <li>Mapper未配置</li> {@link Options#flushCache()}
     *     <li>未执行update操作</li>
     *     <li>缓存作用域不是 STATEMENT</li> mybatis-config.xml中配置 <br> 不是关闭一级缓存，而是缩小的作用域，在嵌套查询的时候会用到 <br> 子查询不会清空
     * </ol>
     */
    @Test
    public void test2() {
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        User userOne = userMapper.selectByIdTwo(10);
        // UserMapper 配置了 @Options(flushCache = Options.FlushCachePolicy.TRUE) 运行时会清空缓存
        userMapper.selectByIdOne(10);
        // 清除了缓存
        userMapper.setName(10, "道友");
        User userTwo = userMapper.selectByIdTwo(10);
        assertSame(userOne, userTwo);
    }

    @Test
    public void test3() {
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        User userOne = userMapper.selectByIdTwo(10);
        User userTwo = userMapper.selectByIdOne(10);
        assertSame(userOne, userTwo);
    }

    @Test
    public void test4() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
        UserMapper usermapper = context.getBean(UserMapper.class);
        // Mapper（动态代理） --> SqlSessionTemplate --> SqlSessionInterceptor（动态代理）-->SqlSessionFactory
        DataSourceTransactionManager transactionManager =
                (DataSourceTransactionManager) context.getBean("txManager");
        // 手动开启事物
        // 在同一个事物的情况下，不会创建新的会话（事物提交、回滚之前）
        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        // 每次都会构造新的会话，导致两次查询的结果不一样，没有走一级缓存（一级缓存是会话级的）
        User userOne = usermapper.selectByIdOne(10);
        // 提交事物
        transactionManager.commit(transactionStatus);
        User userTwo = usermapper.selectByIdOne(10);
        assertSame(userOne, userTwo);
//        // 提交事物
//        transactionManager.commit(transactionStatus);
    }

}