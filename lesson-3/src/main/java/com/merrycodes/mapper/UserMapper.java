package com.merrycodes.mapper;

import com.merrycodes.model.User;
import com.merrycodes.operation.DiskCache;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.cache.decorators.FifoCache;

/**
 * @author MerryCodes
 * @date 2020/6/8 9:40
 */
// 二级缓存命名空间
@CacheNamespace
//@CacheNamespace(readWrite = false) // 不序列化，默认为true
//@CacheNamespace(eviction = FifoCache.class,size = 10) // 溢出淘汰
//@CacheNamespace(implementation = DiskCache.class, properties = {@Property(name = "cachePath", value = "D:\\")}) // 使用自定义存储逻辑
public interface UserMapper {

    @Select({" select * from user where id = #{1}"})
    // 不走二级缓存
//    @Options(useCache = false)
    // 清空缓存
//    @Options(flushCache = Options.FlushCachePolicy.TRUE)
    User selectByIdOne(Integer id);

    @Select({" select * from user where id = #{1}"})
    User selectByIdTwo(Integer id);

}
