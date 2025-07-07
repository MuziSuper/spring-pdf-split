package com.shardingSphere.demo.constant;

public class Constant {
    public static final Long LIMIT_10KB=1024L*10;
    public static final Long LIMIT_100KB=1024L*100;
    public static final Long LIMIT_1MB=1024L*1024;
    public static final Long LIMIT_10MB=1024L*1024*10;
    public static final Long LIMIT_100MB=1024L*1024*100;
    public static final Long LIMIT_1GB=1024L*1024*1024;

    public static final String COS_FILE_EXISTS="The file already exists. The file's MD5 hash is: ";
    public static final String COS_FILE_UPLOAD_FAILED="File upload failed. The file's MD5 hash is: ";
    public static final String APPLICATION_PDF="application/pdf";
    public static final String SIZE_AND_PAGE_CANNOT_BE_NULL = "size and page cannot be null at the same time";
    public static final String PAGE_DIVISION_FAILED = "Page division failed.";
    public static final String FILE_NOT_SELECTED = "Please select a file.";
    public static final String FILE_NOT_PDF = "Please select a PDF file.";
    public static final String FILE_NOT_EXIST = "The PDF named '";
    public static final String SINGLE_PAGE_PDF= "' has only one page and cannot be split into multiple pages.";
    public static final String EMPTY_PDF="' is empty and will not be processed.";
    public static final String FILE_NOT_READABLE="The file is unable to retrieve data.";
    public static final String LIMIT_SIZE_TOO_LARGE="Setting the limitSize too large resulted in the pdf named {} not being paginated.";

}
