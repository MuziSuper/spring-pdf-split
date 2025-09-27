package com.shardingSphere.demo.model;

import cn.muzisheng.pear.annotation.PearField;
import cn.muzisheng.pear.annotation.PearObject;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shardingSphere.demo.handler.PDFStatusTypeHandler;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;


@Data
@TableName("pdf_info")
@Entity
public class PDFInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @TableField(value = "gmt_modified",fill= FieldFill.UPDATE)
    private LocalDateTime gmtModified;
    @TableField(value = "gmt_created",fill= FieldFill.INSERT_UPDATE)
    private LocalDateTime gmtCreated;
    private String md5;
    private String url;
    @Column(name = "`key`")
    private String key;
    @TableField(value = "user_id")
    private Long userId;
    @TableField(value = "file_name")
    private String fileName;
    @TableField(typeHandler = PDFStatusTypeHandler.class)
    private PDFStatus status;
}