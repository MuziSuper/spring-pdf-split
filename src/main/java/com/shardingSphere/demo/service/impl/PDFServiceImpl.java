package com.shardingSphere.demo.service.impl;

import com.qcloud.cos.transfer.TransferManager;
import com.shardingSphere.demo.constant.Constant;
import com.shardingSphere.demo.exception.FileException;
import com.shardingSphere.demo.exception.IllegalException;
import com.shardingSphere.demo.service.PDFService;
import com.shardingSphere.demo.utils.CosUtil;
import com.shardingSphere.demo.utils.DateUtil;
import com.shardingSphere.demo.utils.ZipUtil;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
public class PDFServiceImpl implements PDFService {

    private final CosUtil cosUtil;
    private static final ThreadLocal<TransferManager> transfer = new ThreadLocal<>();
    Logger LOG = LoggerFactory.getLogger(PDFServiceImpl.class);

    public PDFServiceImpl(CosUtil cosUtil) {
        this.cosUtil = cosUtil;
    }

    @Override
    public ResponseEntity<?> pdfSplitAndZip(Long userId, Long size, Integer page, MultipartFile[] files) {
        // 创建一个map用来保存分页后的文件名与文件二进制内容
        HashMap<String, byte[]> prepare = new HashMap<>();
        if (files.length == 0) {
            throw new FileException(Constant.FILE_NOT_SELECTED);
        }
        // 处理参数
        size = transitionSize(size);
        page = transitionPage(page);
        // 对每个文件进行处理
        for (MultipartFile file : files) {
            System.out.println("File name: " + file.getOriginalFilename());
            System.out.println("File length: " + file.getSize());
            if (!Objects.requireNonNull(file.getContentType()).equals(Constant.APPLICATION_PDF)) {
                throw new FileException(Constant.FILE_NOT_PDF);
            }
//            LOG.info("File name: " + file.getOriginalFilename());
//            LOG.info("File size: " + file.getSize());
//            LOG.info("File type: " + file.getContentType());
            InputStream inputStream;
            String md5;
            // 根据流获取md5
            try {
                inputStream = file.getInputStream();
                md5 = DigestUtils.md5DigestAsHex(inputStream);
                inputStream = file.getInputStream();
            } catch (IOException e) {
                throw new FileException();
            }
            LOG.info("正在将原PDF存储到COS中...");
            // 将原PDF存储到COS
            cosUtil.uploadStream(inputStream, md5, file.getSize(), transfer);

            // 对单个文件进行拆分与存储
            singleFileProcess(file, size, page, prepare);
        }
        byte[] zip = ZipUtil.createZip(prepare);
        cosUtil.shutdownTransferManager(transfer.get());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + userId + "_" + DateUtil.getDateStr() + ".zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(zip.length)
                .body(zip);

    }

    public Integer transitionPage(Integer page) {
        if (page != null) {
            if (page < 1) {
                page = 1;
            }
        }
        return page;
    }

    public Long transitionSize(Long size) {
        if (size != null) {
            if (size < Constant.LIMIT_10KB) {
                size = Constant.LIMIT_10KB;
            } else if (size > Constant.LIMIT_10KB && size <= Constant.LIMIT_100KB) {
                size = Constant.LIMIT_100KB;
            } else if (size > Constant.LIMIT_100KB && size <= Constant.LIMIT_1MB) {
                size = Constant.LIMIT_1MB;
            } else if (size > Constant.LIMIT_1MB && size <= Constant.LIMIT_10MB) {
                size = Constant.LIMIT_10MB;
            } else if (size > Constant.LIMIT_10MB && size <= Constant.LIMIT_100MB) {
                size = Constant.LIMIT_100MB;
            } else if (size > Constant.LIMIT_100MB && size <= Constant.LIMIT_1GB) {
                size = Constant.LIMIT_1GB;
            } else {
                size = Constant.LIMIT_1GB;
            }
        }
        return size;
    }

    private void singleFileProcess(MultipartFile file, Long size, Integer page, HashMap<String, byte[]> prepare) {
        List<byte[]> splitPages;
        String fileName = file.getOriginalFilename();
        LOG.info("正在将PDF拆分并保存到COS中...");
        // 将PDF转换为pdfbox文档
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            LOG.info("原PDF共{}页", document.getNumberOfPages());
            if (document.getNumberOfPages() == 0) {
                LOG.warn(Constant.FILE_NOT_EXIST + "{}" + Constant.EMPTY_PDF, fileName);
                return;
            }
            if (document.getNumberOfPages() == 1) {
                LOG.warn(Constant.FILE_NOT_EXIST + "{}" + Constant.SINGLE_PAGE_PDF, fileName);
                return;
            }
            // 根据页数分页
            if (page != null) {
                splitPages = splitByPageCount(document, page);
                // 根据大小分页
            } else if (size != null) {
                splitPages = splitBySize(document, size);
            } else {
                LOG.error(Constant.SIZE_AND_PAGE_CANNOT_BE_NULL);
                throw new IllegalException(Constant.SIZE_AND_PAGE_CANNOT_BE_NULL);
            }
        } catch (IOException e) {
            throw new FileException();
        }
        LOG.info("PDF已分页，共{}页", splitPages.size());
        // 如果就处理后就还是一个pdf说明limitSize过大，直接返回原文件数据
        if (splitPages.size() == 1) {
            byte[] bytes;
            try {
                bytes = file.getBytes();
            } catch (IOException e) {
                throw new FileException(Constant.FILE_NOT_READABLE);
            }
            fileName = Objects.requireNonNull(file.getOriginalFilename()).replaceAll(".pdf", "") + ".pdf";
            prepare.put(fileName, bytes);
            LOG.warn(Constant.LIMIT_SIZE_TOO_LARGE, fileName);
        } else {
            // 存入分页后文档的名称与文档二进制数据
            for (int i = 0; i < splitPages.size(); i++) {
                byte[] bytes = splitPages.get(i);
                fileName = Objects.requireNonNull(file.getOriginalFilename()).replaceAll(".pdf", "") + "_" + (i + 1) + ".pdf";
                prepare.put(fileName, bytes);
            }
            // 存储分页后的所有pdf
            for (int i = 0;i<splitPages.size();i++) {
                // 创建md5
                String md5 = DigestUtils.md5DigestAsHex(splitPages.get(i));
                LOG.info("第{}/{}分页中...", i+1, splitPages.size());
                // 上传分页后的pdf到COS
                try (ByteArrayInputStream bais = new ByteArrayInputStream(splitPages.get(i))) {
                    cosUtil.uploadStream(bais, md5, splitPages.get(i).length, transfer);
                } catch (IOException e) {
                    throw new FileException();
                }
            }
        }
    }

    private List<byte[]> splitByPageCount(PDDocument document, Integer page) {
        Splitter splitter = new Splitter();
        splitter.setSplitAtPage(page);
        try {
            return splitDocuments(splitter.split(document));
        } catch (IOException e) {
            throw new FileException(Constant.PAGE_DIVISION_FAILED);
        }

    }

    /**
     * 按分片大小分页，确保每个分片的大小不超过sizeLimit
     *
     * @param document  原PDF文档
     * @param sizeLimit 分片大小限制
     */
    public List<byte[]> splitBySize(PDDocument document, long sizeLimit) throws IOException {
        // 最终pdf分页的多个pdf二进制数据
        List<byte[]> result = new ArrayList<>();
        // 临时存储的当前未超过size的所有单页pdf,用于累计与整合
        List<PDDocument> currentChunk = new ArrayList<>();
        // 临时总分页大小
        long tempAllSize = 0;
        // 遍历当前pdf所有页
        for (int i = 0; i < document.getNumberOfPages(); i++) {
            try (PDDocument singleDoc = new PDDocument()) {
                // 单页文档添加当前页面
                singleDoc.addPage(document.getPage(i));
                // 测试保存大小
                ByteArrayOutputStream singleStream = new ByteArrayOutputStream();
                singleDoc.save(singleStream);
                // 计算当页大小
                long singleDocSize = singleStream.size();
                // 如果加入当前页，临时总分页大小超出则进行处理，否则放入临时总分页中，累加临时总分页大小
                if (tempAllSize + singleDocSize > sizeLimit) {
                    // 如果临时总分页不为空，说明临时总分页不止当前页，对临时总分页进行保存后清空
                    if (!currentChunk.isEmpty()) {
                        result.add(savePagesToBytes(currentChunk));
                        tempAllSize = 0;
                        currentChunk.clear();
                    }
                    // 如果当前页大于限制直接放入临时总分页中保存并清空，否则放入临时总分页中，累加临时总分页大小
                    if (singleDocSize > sizeLimit) {
                        currentChunk.add(singleDoc);
                        result.add(savePagesToBytes(currentChunk));
                        currentChunk.clear();
                        tempAllSize = 0;
                        LOG.warn("第 {} 页大小({} bytes)超过分片限制({} bytes),不予处理", i + 1, singleDocSize, sizeLimit);
                    } else {
                        // 当前页没超出大小，缓存当前页
                        currentChunk.add(singleDoc);
                        tempAllSize += singleDocSize;
                    }
                } else {
                    // 可以添加到当前分片
                    try (PDDocument pageDoc = new PDDocument()) {
                        pageDoc.addPage(document.getPage(i));
                        currentChunk.add(pageDoc);
                        tempAllSize += singleDocSize;
                    }
                }
            }
        }
        // 如果遍历结束后，临时总分页不为空，说明还有多余页面对其保存
        if (!currentChunk.isEmpty()) {
            result.add(savePagesToBytes(currentChunk));
        }
        return result;
    }

    /**
     * 将多个单页文档合并保存为一个字节数组
     */
    private byte[] savePagesToBytes(List<PDDocument> pages) throws IOException {
        try (PDDocument mergedDoc = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            for (PDDocument pageDoc : pages) {
                mergedDoc.addPage(pageDoc.getPage(0));
            }

            mergedDoc.save(baos);
            return baos.toByteArray();
        } finally {
            // 关闭所有单页文档
            for (PDDocument doc : pages) {
                doc.close();
            }
        }
    }

    private List<byte[]> splitDocuments(List<PDDocument> documents) throws IOException {
        List<byte[]> result = new ArrayList<>();
        for (PDDocument doc : documents) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                doc.save(baos);
                result.add(baos.toByteArray());
            } finally {
                doc.close();
            }
        }
        return result;
    }

}
