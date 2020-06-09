package com.merrycodes.mapper;

import com.merrycodes.model.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author MerryCodes
 * @date 2020/6/8 9:40
 */
// 二级缓存命名空间
//@CacheNamespace
public interface UserMapper {

    @Select({" select * from user where id = #{1}"})
//    @Options(flushCache = Options.FlushCachePolicy.TRUE)
    User selectByIdOne(Integer id);

    @Select({" select * from user where id = #{1}"})
    User selectByIdTwo(Integer id);

    @Update("update  user set name=#{arg1} where id=#{arg0}")
    Integer setName(Integer id, String name);

    @Insert("INSERT INTO `user`( `name`, `age`, `sex`, `email`, `phone_number`) VALUES ( #{name}, #{age}, #{sex}, #{email}, #{phoneNumber})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Integer addUser(User user);


}
