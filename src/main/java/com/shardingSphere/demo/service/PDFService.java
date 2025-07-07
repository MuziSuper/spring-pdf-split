package com.shardingSphere.demo.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface PDFService {
    ResponseEntity<?> pdfSplitAndZip(Long userId, Long size, Integer page, MultipartFile[] files);

}
