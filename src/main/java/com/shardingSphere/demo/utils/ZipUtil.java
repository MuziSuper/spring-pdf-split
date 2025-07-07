package com.shardingSphere.demo.utils;

import com.shardingSphere.demo.exception.FileException;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

    public static byte[] createZip(Map<String,byte[]> map) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Map.Entry<String, byte[]> entry:map.entrySet()) {
                ZipEntry zip = new ZipEntry(entry.getKey());
                zos.putNextEntry(zip);
                zos.write(entry.getValue());
                zos.closeEntry();
            }
            zos.finish();
            return baos.toByteArray();
        }catch (Exception e) {
            throw new FileException("Failed to create zip file");
        }
    }
}