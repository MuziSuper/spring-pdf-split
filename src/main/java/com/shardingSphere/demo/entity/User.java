package com.shardingSphere.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String avatar;
    private String nickname;
    private LocalDateTime gmtCreated;
    private LocalDateTime gmtModified;
}
