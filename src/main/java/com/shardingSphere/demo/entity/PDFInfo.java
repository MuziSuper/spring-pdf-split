package com.shardingSphere.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PDFInfo {
    private Long id;
    private String name;
    private String md5;
    private String userId;
    private String size;
    private Integer page;
    private String path;
    private LocalDateTime gmtCreated;
    private LocalDateTime gmtModified;
}
