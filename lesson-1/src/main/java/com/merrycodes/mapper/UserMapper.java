package com.merrycodes.mapper;

import com.merrycodes.model.User;
import org.apache.ibatis.annotations.*;

/**
 * @author MerryCodes
 * @date 2020/6/5 21:13
 */
// 二级缓存命名空间
//@CacheNamespace
public interface UserMapper {

    @Select({" select * from user where id = #{1}"})
    User selectById(Integer id);

    @Update("update  user set name = #{arg1} where id = #{arg0}")
    Integer setName(Integer id, String name);

}
