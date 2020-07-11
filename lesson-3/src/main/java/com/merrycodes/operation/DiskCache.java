package com.merrycodes.operation;

import com.merrycodes.utils.EncryptUtils;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.ibatis.cache.Cache;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * @author MerryCodes
 * @date 2020/6/15 9:35
 */
public class DiskCache implements Cache {

    private final String id;

    private String cachePath;

    public DiskCache(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    @SneakyThrows
    public void putObject(Object key, Object value) {
        String MD5Key = EncryptUtils.MD5(toBytes(key));
        Files.write(Paths.get(cachePath + '/' + MD5Key), toBytes(value));
    }

    @Override
    @SneakyThrows
    public Object getObject(Object key) {
        String MD5Key = EncryptUtils.MD5(toBytes(key));
        @Cleanup FileInputStream fileInputStream = new FileInputStream(cachePath + '/' + MD5Key);
        @Cleanup ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        return objectInputStream.readObject();
    }

    @Override
    public Object removeObject(Object key) {
        String MD5Key = EncryptUtils.MD5(toBytes(key));
        File file = new File(cachePath + "/" + MD5Key);
        if (file.exists()) {
            file.delete();
        }
        return null;
    }

    @Override
    public void clear() {

    }

    @Override
    public int getSize() {
        return Objects.requireNonNull(new File(cachePath).list()).length;
    }

    public String getCachePath() {
        return cachePath;
    }

    public void setCachePath(String cachePath) {
        this.cachePath = cachePath;
    }

    @SneakyThrows
    private static byte[] toBytes(Object object) {
        @Cleanup ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        @Cleanup ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(object);
        return byteArrayOutputStream.toByteArray();
    }

}
