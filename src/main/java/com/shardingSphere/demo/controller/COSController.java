package com.shardingSphere.demo.controller;

import com.shardingSphere.demo.utils.CosUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cos")
public class COSController {
    private CosUtil cosUtil;
    public COSController(CosUtil cosUtil){
        this.cosUtil = cosUtil;
    }
    @PostMapping("/create")
    public void create(){

    }
}
