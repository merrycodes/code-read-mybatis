package com.merrycodes.operation;

import com.merrycodes.mapper.UserMapper;
import org.apache.ibatis.executor.CachingExecutor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ReuseExecutor;
import org.apache.ibatis.executor.SimpleExecutor;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.jdbc.JdbcTransaction;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * @author MerryCodes
 * @date 2020/6/6 0:04
 */
public class ExecutorTest {

    private Configuration configuration;
    private MappedStatement mappedStatement;
    private JdbcTransaction jdbcTransaction;
    private SqlSessionFactory sqlSessionFactory;

    @Before
    public void init() throws IOException {
        // 获取构建器
        SqlSessionFactoryBuilder factoryBuilder = new SqlSessionFactoryBuilder();
        // 解析XML文件，并构造会话工厂
        sqlSessionFactory = factoryBuilder.build(Resources.getResourceAsStream("mybatis-config.xml"));
        configuration = sqlSessionFactory.getConfiguration();
        jdbcTransaction = new JdbcTransaction(sqlSessionFactory.openSession().getConnection());
        // 获取SQL映射
        mappedStatement = configuration.getMappedStatement("com.merrycodes.mapper.UserMapper.selectById");
    }

    // 简单执行器测试
    @Test
    public void simpleTest() throws SQLException {
        SimpleExecutor simpleExecutor = new SimpleExecutor(configuration, jdbcTransaction);
        List<Object> list = simpleExecutor.doQuery(mappedStatement, 10, RowBounds.DEFAULT,
                SimpleExecutor.NO_RESULT_HANDLER, mappedStatement.getBoundSql(10));
        // 再编译一次运行
        simpleExecutor.doQuery(mappedStatement, 10, RowBounds.DEFAULT,
                SimpleExecutor.NO_RESULT_HANDLER, mappedStatement.getBoundSql(10));
        System.out.println(list.get(0));
    }

    // 重用执行器测试
    @Test
    public void reuseTest() throws SQLException {
        ReuseExecutor reuseExecutor = new ReuseExecutor(configuration, jdbcTransaction);
        List<Object> list = reuseExecutor.doQuery(mappedStatement, 10, RowBounds.DEFAULT,
                SimpleExecutor.NO_RESULT_HANDLER, mappedStatement.getBoundSql(10));
        // 相同的SQL 会缓存对应的 PreparedStatement 会话级的
        // 底层使用一个Map存储 --> statementMap
        reuseExecutor.doQuery(mappedStatement, 10, RowBounds.DEFAULT,
                SimpleExecutor.NO_RESULT_HANDLER, mappedStatement.getBoundSql(10));
        System.out.println(list.get(0));
    }

    // 批处理执行器（批处理对修改有效，查询没有效果）
    @Test
    public void BatchTest() throws SQLException {
//        BatchExecutor batchExecutor = new BatchExecutor(configuration, jdbcTransaction);
//        MappedStatement setName = configuration
//                .getMappedStatement("com.merrycodes.mapper.UserMapper.setName");
//        Map<String, Object> param = new HashMap<>();
//        param.put("arg0", 4);
//        param.put("arg1", "merrycodes is good man");
//        batchExecutor.doUpdate(setName, param); //设置
//        batchExecutor.doUpdate(setName, param);// 设置
//        batchExecutor.doFlushStatements(false);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, true);
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        userMapper.setName(13, "merrycodes is good man");
        userMapper.setName(14, "merrycodes is good man");
        sqlSession.flushStatements();
    }

    @Test
    public void cacheExecutorTest() throws SQLException {
        Executor executor = new SimpleExecutor(configuration, jdbcTransaction);
        // 装饰器模式
        Executor cachingExecutor = new CachingExecutor(executor);
        cachingExecutor.query(mappedStatement, 10, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
        //  提交之后才会更新
        cachingExecutor.commit(true);
        cachingExecutor.query(mappedStatement, 10, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
    }

    @Test
    public void sessionTest() {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.REUSE, true);
        // 降低调用复杂性
        List<Object> list = sqlSession.selectList("com.merrycodes.mapper.UserMapper.selectById", 10);
        sqlSession.commit();
        sqlSession.selectList("com.merrycodes.mapper.UserMapper.selectById", 10);
        System.out.println(list.get(0));
    }


}