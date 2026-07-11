package com.hexaware.cozy_heaven.dto;

import com.hexaware.cozy_heaven.model.Role;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDTO {

    private String token;

    private Integer userId;

    private String email;
    
    private String name;

    private Role role;
}