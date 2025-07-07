package com.shardingSphere.demo.utils;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.*;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.transfer.*;
import com.shardingSphere.demo.constant.Constant;
import com.shardingSphere.demo.exception.COSException;
import com.shardingSphere.demo.properties.COSProperties;
import jakarta.annotation.Resource;
import lombok.Locked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipInputStream;

@Service
public class CosUtil {
    private final String secretId;
    private final String secretKey;
    private final String bucketName;
    private final String region;
    private final COSClient cosClient;
    private static final Logger LOG = LoggerFactory.getLogger(CosUtil.class);

    public CosUtil(COSProperties cosProperties) {
        this.secretId = cosProperties.getSecretId();
        this.secretKey = cosProperties.getSecretKey();
        this.bucketName = cosProperties.getBucketName();
        this.region = cosProperties.getRegion();
        this.cosClient = createCli();
        LOG.info("bucketName: "+bucketName+" region: "+region+" secretId: "+secretId+" secretKey: "+secretKey);
    }
    private TransferManager createTransferManager() {
        // 3 生成cos客户端
        COSClient cosclient = createCli();
        ExecutorService threadPool = Executors.newFixedThreadPool(32);
        // 传入一个threadpool, 若不传入线程池, 默认TransferManager中会生成一个单线程的线程池。
        TransferManager transferManager=new TransferManager(cosclient, threadPool);
        TransferManagerConfiguration transferManagerConfiguration = new TransferManagerConfiguration();
        transferManagerConfiguration.setMultipartUploadThreshold(1024 * 1024 * 5);
        transferManagerConfiguration.setMinimumUploadPartSize(1024 * 1024);
        transferManager.setConfiguration(transferManagerConfiguration);
        return transferManager;
    }
    private COSClient createCli() {
        // 1 初始化用户身份信息(appid, secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        // 2 设置bucket的区域, COS地域的简称请参照 https://www.qcloud.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        // 3 生成cos客户端
        return new COSClient(cred, clientConfig);
    }
    // 上传文件, 根据文件大小自动选择简单上传或者分块上传。
    public String uploadFile(File file,String md5) {
        TransferManager transferManager = createTransferManager();


        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, md5, file);
        try {
            boolean objectExists = cosClient.doesObjectExist(bucketName, md5);
            if (objectExists) {
                LOG.info(Constant.COS_FILE_EXISTS+md5);
                return getFileUrl(md5);
            }
            // 返回一个异步结果Upload, 可同步的调用waitForUploadResult等待upload结束, 成功返回UploadResult, 失败抛出异常.
            long startTime = System.currentTimeMillis();
            // 返回一个异步结果Upload, 可同步的调用waitForUploadResult等待upload结束, 成功返回UploadResult, 失败抛出异常.
            Upload upload = transferManager.upload(putObjectRequest);
            // 控制台显示数据进度
            // showTransferProgress(upload);
            // 阻塞等待获取上传结果
            UploadResult uploadResult = upload.waitForUploadResult();
            long endTime = System.currentTimeMillis();
            LOG.info("used time: " + (endTime - startTime) / 1000);
            LOG.info(uploadResult.getETag());
            LOG.info(uploadResult.getCrc64Ecma());
        } catch (InterruptedException | CosClientException e) {
            LOG.error(Constant.COS_FILE_UPLOAD_FAILED+md5);
            throw new COSException();
        }
        return getFileUrl(md5);
    }
    // 上传文件, 根据文件大小自动选择简单上传或者分块上传。
    public String uploadStream(InputStream stream,String md5,long size,ThreadLocal<TransferManager> transfer) {
        TransferManager transferManager = createTransferManager();
        transfer.set(transferManager);
        ObjectMetadata objectMetadata = new ObjectMetadata();
        PutObjectRequest putObjectRequest;
        try {
            boolean objectExists = cosClient.doesObjectExist(bucketName, md5);
            if (objectExists) {
                LOG.info(Constant.COS_FILE_EXISTS + md5);
                return getFileUrl(md5);
            }
            putObjectRequest = new PutObjectRequest(bucketName, md5,stream, objectMetadata);
            objectMetadata.setContentLength(size);
            objectMetadata.setContentType(Constant.APPLICATION_PDF);
            // 返回一个异步结果Upload, 可同步的调用waitForUploadResult等待upload结束, 成功返回UploadResult, 失败抛出异常.
            long startTime = System.currentTimeMillis();
            // 非阻塞上传
            Upload upload = transferManager.upload(putObjectRequest);
            // 控制台显示数据进度
            // showTransferProgress(upload);
            // 阻塞等待获取上传结果
            UploadResult uploadResult = upload.waitForUploadResult();
            long endTime = System.currentTimeMillis();
            LOG.info("md5: {} 上传完成，响应信息如下：",md5);
            LOG.info("RequestId: {}", uploadResult.getRequestId());
            LOG.info("used time: " + (endTime - startTime) / 1000);
            LOG.info("Etage: "+uploadResult.getETag());
            LOG.info("Crc64Ecma: "+uploadResult.getCrc64Ecma());
        } catch (InterruptedException | CosClientException e) {
            LOG.error(Constant.COS_FILE_UPLOAD_FAILED + "{}", md5);
            throw new COSException();
        }
        return getFileUrl(md5);
    }
    public String getFileUrl(String key){
       COSClient cosClient = createCli();
       URL url= cosClient.getObjectUrl(bucketName, key);
       return url.toString();
    }
    public void shutdownTransferManager(TransferManager transferManager) {
        // 指定参数为 true, 则同时会关闭 transferManager 内部的 COSClient 实例。
        // 指定参数为 false, 则不会关闭 transferManager 内部的 COSClient 实例。
        transferManager.shutdownNow(true);
    }
    //在等待转移完成时打印进度
    private static void showTransferProgress(Transfer transfer) {
        LOG.info(transfer.getDescription());
        do {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return;
            }
            TransferProgress progress = transfer.getProgress();
            long so_far = progress.getBytesTransferred();
            long total = progress.getTotalBytesToTransfer();
            double pct = progress.getPercentTransferred();
            System.out.printf("[%d / %d] = %.02f%%\n", so_far, total, pct);
        } while (!transfer.isDone());
        LOG.info(String.valueOf(transfer.getState()));
    }
}
