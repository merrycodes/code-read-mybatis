package com.merrycodes.operation;

import lombok.Cleanup;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.*;
import java.util.UUID;

/**
 * @author MerryCodes
 * @date 2020/6/5 21:44
 */
public class JdbcTest {

    private static final String URL = "jdbc:mysql://localhost:3306/code-read?serverTimezone=Asia/Shanghai";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "123456";
    private Connection connection;

    @Before
    public void init() throws SQLException {
        connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    @After
    public void close() throws SQLException {
        connection.close();
    }

    @Test
    public void jdbcTest() throws SQLException {
        // 预编译
        String sql = "SELECT * FROM user WHERE `name` = ?";
        @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
        // 设置参数
        preparedStatement.setString(1, "merrycodes");
        preparedStatement.execute();
        // 获取结果集
        @Cleanup ResultSet resultSet = preparedStatement.getResultSet();
        while (resultSet.next()) {
            System.out.println(resultSet.getString(1));
        }
    }

    @Test
    public void prepareBatchTest() throws SQLException {
        String sql = "INSERT INTO `user` (`name`,age) VALUES (?,18);";
        @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setFetchSize(100);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            // 设置参数
            preparedStatement.setString(1, UUID.randomUUID().toString());
//            preparedStatement.execute(); // 单条执行
//            preparedStatement.addBatch(sql); // 添加批处理
            preparedStatement.addBatch(); // 添加批处理
        }
        preparedStatement.executeBatch(); // 批处理
        System.out.println(System.currentTimeMillis() - start);
    }

    // Statement 不能防止SQL注入
    public int selectByName(String name) throws SQLException {
        String sql = "SELECT * FROM user WHERE `name`='" + name + "'";
        System.out.println(sql);
        @Cleanup Statement statement = connection.createStatement();
        statement.executeQuery(sql);
        @Cleanup ResultSet resultSet = statement.getResultSet();
        int count = 0;
        while (resultSet.next()) {
            count++;
        }
        return count;
    }

    //PreparedStatement防止SQL注入测试
    public int selectByNamePreventInjection(String name) throws SQLException {
        String sql = "SELECT * FROM user WHERE `name`= ? ";
        // 转义（参数转义发生在数据库端）
        @Cleanup PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, name);
        System.out.println(statement);
        statement.executeQuery();
        @Cleanup ResultSet resultSet = statement.getResultSet();
        int count = 0;
        while (resultSet.next()) {
            count++;
        }
        return count;
    }

    // sql注入测试
    @Test
    public void injectTest() throws SQLException {
        System.out.println(selectByName("835b888c-bf8f-4623-bc27-0c542cf98262"));
        System.out.println(selectByName("835b888c-bf8f-4623-bc27-0c542cf98262' or '1'='1"));
        // 防止SQL注入
        System.out.println(selectByNamePreventInjection("835b888c-bf8f-4623-bc27-0c542cf98262' or '1'='1"));
    }

    // PreparedStatement 一次编译多次执行
    public void prepareTest() throws SQLException {
        String sql = "SELECT * FROM user WHERE `name` = ?";
        @Cleanup PreparedStatement statement = connection.prepareStatement(sql);
        // 第一次
        statement.setString(1, "7fd0ac7d-5ca0-46ed-9ec7-920d06743e1e");
        statement.executeQuery();
        statement.getResultSet();
        //第二次
        statement.setString(1, "a11e1fc7-c234-4a03-8c97-05347707372c");
        statement.executeQuery();
    }

}