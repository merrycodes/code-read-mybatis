package com.merrycodes.model;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author MerryCodes
 * @date 2020/6/5 21:13
 */
@Data
public class User implements Serializable {

    private Integer id;

    private String name;

    private String age;

    private String sex;

    private String email;

    private String phoneNumber;

    private Date createTime;

}

