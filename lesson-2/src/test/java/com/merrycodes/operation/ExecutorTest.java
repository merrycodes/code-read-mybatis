package com.merrycodes.operation;

import com.merrycodes.mapper.UserMapper;
import com.merrycodes.model.User;
import com.merrycodes.utils.MockUtils;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * @author MerryCodes
 * @date 2020/6/8 9:41
 */
public class ExecutorTest {

    private SqlSessionFactory sqlSessionFactory;

    @Before
    public void init() throws IOException {
        SqlSessionFactoryBuilder factoryBuilder = new SqlSessionFactoryBuilder();
        sqlSessionFactory = factoryBuilder.build(Resources.getResourceAsStream("mybatis-config.xml"));
    }

    // 使用重用执行器
    @Test
    public void sessionReuseTest() {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.REUSE, true);
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        // SQL 语句一样，MappedStatement 不一样
        // MappedStatement : com.merrycodes.mapper.UserMapper#selectByIdOne
        userMapper.selectByIdOne(10);
        // MappedStatement : com.merrycodes.mapper.UserMapper#selectByIdTwo
        userMapper.selectByIdTwo(10);
        // 结论：在使用重用执行器的情况下，SQL一样则都会使用用一个 JDBC statement，底层维护了一个 Map<String, Statement>
        // " select * from user where id = #{1}" 和 "select * from user where id = #{1}" JDBC statement重用（后者‘select’前没有空格）
        // " select * from user where id = #{1}" 和 " select * from user where id =#{1}" JDBC statement 不重用（后者‘=’后没有空格）
    }

    // 1.添加
    // 2.执行
    // 3.返回结果
    @Test
    public void sessionBatchTest() {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, true);
        UserMapper usermapper = sqlSession.getMapper(UserMapper.class);
        // SQL语句相同
        // MappedStatement相同
        // 必须连续的
        usermapper.setName(10, "道友友谊永存");
        User user = MockUtils.mockUser();
        usermapper.addUser(user);
        usermapper.addUser(user);
        usermapper.setName(10, "道友");
        // 使用不同的 JDBC statement
        // 一起提交
        List<BatchResult> batchResults = sqlSession.flushStatements();
        System.out.println(batchResults.size());

    }


}