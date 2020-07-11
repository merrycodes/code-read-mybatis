package com.merrycodes.model;


import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import java.io.Serializable;
import java.util.Date;

/**
 * @author MerryCodes
 * @date 2020/6/8 9:40
 */
@Data
@Builder
public class User implements Serializable {

    private Integer id;

    private String name;

    private String age;

    private String sex;

    private String email;

    private String phoneNumber;

    private Date createTime;

    @Tolerate
    public User() {
    }

}

