package com.aeris2.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserRequest {
    private String name;
    private String phone;
    private String address;
    private String password;
}
