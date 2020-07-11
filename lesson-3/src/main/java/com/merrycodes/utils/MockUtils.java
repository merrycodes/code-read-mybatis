package com.merrycodes.utils;

import com.merrycodes.model.User;

/**
 * @author MerryCodes
 * @date 2020/6/8 10:04
 */
public class MockUtils {

    public static User mockUser() {
        String prefix = String.valueOf(System.currentTimeMillis());
        prefix = prefix.substring(prefix.length() - 5);
        return User.builder()
                .name("mock_" + prefix)
                .age("18")
                .build();
    }

}
