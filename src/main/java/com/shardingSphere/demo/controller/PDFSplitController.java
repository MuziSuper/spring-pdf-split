package com.shardingSphere.demo.controller;

import com.shardingSphere.demo.service.PDFService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/pdf")
public class PDFSplitController {
    private final PDFService pdfService;

    public PDFSplitController(PDFService pdfService) {
        this.pdfService = pdfService;
    }
    @PostMapping("/split/{userId}")
    public ResponseEntity<?> split(@PathVariable(name = "userId", required = false) Long userId,
                                          @RequestParam(value = "size", required = false) Long size,
                                          @RequestParam(value = "page", required = false) Integer page,
                                          @RequestParam("files") MultipartFile[] files) {
        return pdfService.pdfSplitAndZip(userId, size, page, files);

    }
}
