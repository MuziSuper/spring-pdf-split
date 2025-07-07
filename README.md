# Spring PDF Split 项目技术文档

## 1. 项目概述

### 背景与目标
`spring-pdf-split` 是一个基于 **Spring Boot** 的轻量级文件拆分服务，主要用于解决大体积 PDF 文件在传输、存储和访问时的性能问题。该项目通过将大 PDF 括展为多个较小的 PDF 文件，并最终打包成 ZIP 格式返回给客户端，从而提升用户体验。

- **主要用户**：需要处理大体积 PDF 文件并进行分布式存储和下载的用户。
- **核心功能**：
  - 支持按**页数**或**文件大小**对 PDF 进行拆分。
  - 使用 `ZIP` 压缩格式将拆分后的文件打包返回。
  - 将原始文件及拆分后文件上传至 **腾讯云 COS（Cloud Object Storage）** 存储。
  
---

## 2. 技术架构与选型

### 2.1 整体架构
本项目采用典型的 **Spring Boot 单体架构**，代码结构清晰，模块化设计良好，包括以下核心组件：

| 模块 | 说明 |
|------|------|
| `Controller` | 接收 HTTP 请求，处理请求参数 |
| `Service` | 执行业务逻辑 |
| `Utils` | 工具类封装 |
| `Entity` | 数据模型定义 |
| `Exception` | 异常处理体系 |
| `Properties` | 配置读取 |
| `Model` | 返回值包装 |
| [Constant](file:///Applications/LocalGit/spring-pdf-split/src/main/java/com/shardingSphere/demo/constant/Constant.java#L2-L23) | 常量定义 |

### 2.2 技术栈
| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.3.4 | 构建 Web 应用框架 |
| Apache PDFBox | 3.0.5 | PDF 文件解析与拆分 |
| Tencent COS SDK | com.qcloud cos_api 5.6.227 | 文件云端存储 |
| ZipUtil | JDK 内置 API | ZIP 文件压缩 |
| Java | 21 | 编程语言 |
| Maven | 3.x | 构建工具 |
| MySQL + MyBatis + ShardingSphere-JDBC | 8.0.33 | 数据库操作 |
| Lombok | - | 简化 POJO 类编写 |
| Thymeleaf / HTML5 | - | 简单前端页面展示 |

---

## 3. 核心业务流程

### 3.1 功能描述

#### 主要接口
- **URL**: `/pdf/split/{userId}`
- **方法**: `POST`
- **参数说明**:
  - [userId](file:///Applications/LocalGit/spring-pdf-split/src/main/java/com/shardingSphere/demo/entity/PDFInfo.java#L15-L15): 用户 ID（路径参数）
  - [size](file:///Applications/LocalGit/spring-pdf-split/src/main/java/com/shardingSphere/demo/entity/PDFInfo.java#L16-L16): 按文件大小拆分（单位：字节）
  - [page](file:///Applications/LocalGit/spring-pdf-split/src/main/java/com/shardingSphere/demo/entity/PDFInfo.java#L17-L17): 按每份文件的页数拆分
  - `files[]`: 多个 PDF 文件上传

> ⚠️ 注意：[size](file:///Applications/LocalGit/spring-pdf-split/src/main/java/com/shardingSphere/demo/entity/PDFInfo.java#L16-L16) 和 [page](file:///Applications/LocalGit/spring-pdf-split/src/main/java/com/shardingSphere/demo/entity/PDFInfo.java#L17-L17) 参数不可同时为空。

#### 业务逻辑流程
1. **接收请求**：由 [PDFSplitController](file:///Applications/LocalGit/spring-pdf-split/src/main/java/com/shardingSphere/demo/controller/PDFSplitController.java#L8-L24) 接收客户端上传的多个 PDF 文件以及拆分策略（页数或大小）。
2. **校验文件**：
   - 检查是否有文件被上传；
   - 文件是否为 PDF 格式；
   - 获取文件 MD5 并判断是否已存在 COS 中。
3. **文件拆分**：
   - 使用 **Apache PDFBox** 对每个 PDF 文件执行拆分操作。
   - 支持两种拆分方式：
     - **按页数拆分**：如每份文件包含 10 页；
     - **按大小拆分**：如每个文件不超过 1MB。
4. **文件上传 COS**：
   - 将原始文件和拆分后的子文件上传到 **腾讯云 COS**。
   - 利用 [CosUtil](file:///Applications/LocalGit/spring-pdf-split/src/main/java/com/shardingSphere/demo/utils/CosUtil.java#L30-L159) 工具类封装上传逻辑。
5. **压缩打包**：
   - 使用 [ZipUtil](file:///Applications/LocalGit/spring-pdf-split/src/main/java/com/shardingSphere/demo/utils/ZipUtil.java#L10-L27) 工具类将所有拆分后的 PDF 文件打包为 ZIP 格式。
6. **响应客户端**：
   - 返回 ZIP 文件流供客户端下载。

---

## 4. 关键技术实现细节

### 4.1 PDF 文件拆分（PDFServiceImpl）

使用 **Apache PDFBox** 实现 PDF 分割功能，主要步骤如下：

```
java
// 按页数拆分
private List<byte[]> splitByPageCount(PDDocument document, Integer page) {
Splitter splitter = new Splitter();
splitter.setSplitAtPage(page);
return splitDocuments(splitter.split(document));
}

// 按大小拆分
public List<byte[]> splitBySize(PDDocument document, long sizeLimit) throws IOException {
...
}
```
- **splitByPageCount**: 使用 `Splitter` 设置每份文件页数；
- **splitBySize**: 自动累加页数直到达到指定大小限制，确保不超出设定的文件大小。

### 4.2 COS 文件上传（CosUtil）

封装了腾讯云 COS 的上传逻辑，支持：

- 文件是否存在检查；
- 大文件自动分片上传；
- 文件上传进度监控；
- 上传成功后生成可访问的 URL。

```
java
public String uploadStream(InputStream stream, String md5, long size, ThreadLocal<TransferManager> transfer)
```
- 使用 `TransferManager` 管理上传任务；
- 支持并发上传；
- 使用线程本地变量管理资源。

### 4.3 ZIP 打包（ZipUtil）

利用 Java 内置的 `ZipOutputStream` 实现多文件压缩：

```
java
public static byte[] createZip(Map<String, byte[]> map) {
try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
ZipOutputStream zos = new ZipOutputStream(baos)) {
for (Map.Entry<String, byte[]> entry : map.entrySet()) {
ZipEntry zip = new ZipEntry(entry.getKey());
zos.putNextEntry(zip);
zos.write(entry.getValue());
zos.closeEntry();
}
zos.finish();
return baos.toByteArray();
} catch (Exception e) {
throw new FileException("Failed to create zip file");
}
}
```
---

## 5. 项目部署与运行

### 5.1 依赖环境

- **JDK 21**
- **Maven 3.x**
- **MySQL 8.0**
- **COS 账号配置**

### 5.2 构建命令

```
bash
mvn clean package
```
### 5.3 启动方式

- 直接运行 [DemoApplication.java](file:///Applications/LocalGit/spring-pdf-split/src/main/java/com/shardingSphere/demo/DemoApplication.java)；
- 或使用命令启动：

```
bash
java -jar spring-pdf-split.jar
```
### 5.4 配置项（application.properties）

```
properties
tencent.cos.secretId=${SECRETID}
tencent.cos.secretKey=${SECRETKEY}
tencent.cos.bucket-name=${BUCKETNAME}
tencent.cos.region=${REGION}
```
---

## 6. 已知问题与优化建议

### 6.1 性能优化方向

- 当前为单机处理模式，可考虑引入 **异步队列** 或 **分布式处理架构**（如 RabbitMQ/Kafka）提高并发能力。
- 可进一步优化内存使用，避免大文件一次性加载。
- 可增加缓存机制，避免重复上传相同文件。

### 6.2 安全性增强

- COS 密钥应通过 **加密配置中心** 或 **环境变量注入** 方式获取，避免明文暴露。
- 可加入 **限流机制**，防止恶意请求压垮服务器。

### 6.3 扩展方向

- 对数据存储如用户操作pdf拆分记录存储到Mysql中，并使用shardingSphere实现数据分库分表，这也是该项目最初的方向。
---

## 7. 总结

本项目是一个结构清晰、功能完整的 PDF 文件拆分服务，适用于需要处理大体积 PDF 文件的场景。结合 Spring Boot、PDFBox、ZIP 打包和 COS 云存储，提供了高效、稳定的解决方案。未来可进一步扩展为微服务架构，以应对更大规模的文件处理需求。

---

