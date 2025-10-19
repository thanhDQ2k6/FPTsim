package com.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AccountView {
    private String id;       // use email as stable id
    private String email;
    private String fullName;
    private String phone;
    private String role;
}


