<div align="center">
  <p>
    <img src="logo.jpg"  height="200px" />
  </p>
  <p>山海Guard - 基于SpringBoot 的通用Web安全组件</p>
  <p>ShanHaiGuard-based SpringBoot Web Security components</p>
  <p>
    <a href="https://github.com/SeeMountainSea/shanhai-guard-spring-boot-starter/releases/latest"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SeeMountainSea/shanhai-guard-spring-boot-starter"/></a>
    <a href="https://github.com/SeeMountainSea/shanhai-guard-spring-boot-starter/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SeeMountainSea/shanhai-guard-spring-boot-starter?color=009688"/></a>
    <a href="https://github.com/topics/java"><img alt="GitHub top language" src="https://img.shields.io/github/languages/top/SeeMountainSea/shanhai-guard-spring-boot-starter?color=eb8031"/></a>
    <br>
    <a href="https://github.com/SeeMountainSea/shanhai-guard-spring-boot-starter/find/master"><img alt="GitHub Code Size" src="https://img.shields.io/github/languages/code-size/SeeMountainSea/shanhai-guard-spring-boot-starter?color=795548"/></a>
    <a href="https://github.com/SeeMountainSea/shanhai-guard-spring-boot-starter/find/master"><img alt="GitHub Code Lines" src="https://img.shields.io/tokei/lines/github/SeeMountainSea/shanhai-guard-spring-boot-starter?color=37474F"/></a>
    <a href="https://github.com/SeeMountainSea/shanhai-guard-spring-boot-starter/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SeeMountainSea/shanhai-guard-spring-boot-starter?color=534BAE"/></a>
  </p>
</div>

ShanHaiGuard 主要提供以下能力：

- 支持全局文件上传安全检测 @Ver1.0.0+支持

- 支持单个方法文件上传安全检测 @Ver1.0.0+支持

- 支持SQL注入&XSS注入安全检测 @Ver1.0.0+支持

- 支持Mybatis SQL查询安全审核（1.x仅支持mysql）@Ver1.0.0+支持

- 支持密码复杂度检验 @Ver1.0.0+支持

- 支持拓展SpringBoot POST数据定制化解析，从而实现全局自动数据解析能力(例如数据解码,动态修改数据) @Ver1.0.0+支持

- 支持对SpringBoot配置文件任意参数进行参数加密。默认算法为PBE，同时支持自己扩展解密算法（尚未适配SpringCloud和其他配置中心)  @Ver1.0.0+支持

  注：由于ShanHaiGuard是基于SpringBoot 2.x的，因此可能有部分组件不支持SpringBoot1.x版本的。鉴于官方已经不再更新SpringBoot 1.X，因此不再考虑兼容SpringBoot1.x版本。
  
  

# 1.引入依赖

```xml
        <dependency>
            <groupId>com.wangshanhai.guard</groupId>
            <artifactId>shanhai-guard-spring-boot-starter</artifactId>
            <version>1.0.0</version>
        </dependency>
```

# 2.启用ShanHaiGuard安全防护组件

```java
@Configuration
@EnableShanHaiGuard
public class ShanHaiGuardConfig {
}
```

# 3.组件说明

## 3.1 文件上传检测

配置参数如下

```yaml
shanhai:
  fileguard:
    enable: true  #启用组件
    pathPatterns: #检测范围
      - '/**'
    suffix: jpg,gif,png,ico,bmp,jpeg #文件上传白名单
    logTarce: true  #启用跟踪日志
```

此处的配置为全局性配置，对于单个方法的校验，可以使用@FileGuard添加校验，参考下面样例的使用方式：

1）通过文件后缀校验(GuardType.SUFFIX)

```java
/**
 * 文件上传测试样例
 * @param request
 * @return
 */
@RequestMapping(value = "/file/suffix/upload")
@FileGuard(message = "只能上传图片文件",type = FileGuard.GuardType.SUFFIX,supportedSuffixes = {"png", "jpg", "jpeg"})
@ResponseBody
public String suffix(HttpServletRequest request, MultipartFile file){
    Logger.info("[file-upload-api]-name:{},contentType:{},size:{}",file.getName(),file.getContentType(),file.getSize());
    return "success";
}
```

2)通过二进制文件头检验(GuardType.BYTES)

```java
/**
 * 文件上传测试样例
 * @param request
 * @return
 */
@RequestMapping(value = "/file/bytes/upload")
@FileGuard(message = "只能上传图片文件",type = FileGuard.GuardType.BYTES,supportedFileTypes = {FileType.JPEG,FileType.PNG})
@ResponseBody
public String bytes(HttpServletRequest request, MultipartFile file){
    Logger.info("[file-upload-api]-name:{},contentType:{},size:{}",file.getName(),file.getContentType(),file.getSize());
    return "success";
}
```

**注：单个方法的白名单必须为全局性白名单的子集**

## 3.2 密码复杂度检测

配置参数如下

```yaml
shanhai:
  passwdguard:
    enable: true #启用组件
    minLength: 4 #最小长度
    maxLength: 10 #最大长度
    characterExist: true #包含大小写字母
    numberExist: true #包含数字
    symbolExist: true #包含符号
    keyboardNotExist: true #不包含键盘排序
```

在业务模块可以直接使用密码复杂度检测服务PasswdService，示例如下：

```java
//注入服务
@Autowired
private PasswdService passwdService;
//调用服务方法
passwdService.checkPasswd(passwd)
```

## 3.3 SQL&XSS注入检测

配置参数如下

```yaml
shanhai:
  webguard:
    enable: true #启用组件
    path-patterns: #检测范围
      - '/*'
```

## 3.4 mysql数据安全检测

配置参数如下

```yaml
shanhai:
  mysqlguard:
    enable: true       #启用组件
    where-exist: false  #包含where语句
    limit-exist: false  #包含limit语句
    query-limit: 20000  #limit条数
```

## 3.5 @RequestBody 通用解码组件

配置参数如下

```yaml
shanhai:
  decodebody:
    enable: true #启用组件
```

注：此组件只是做了通用性封装，对于解码逻辑需要自行实现接口DecodeBodyService的decodeRequestBody方法，在进行数据解码时，会自动调用该接口的实现方法进行自定义参数解码操作。

接口定义如下：

```java
public interface DecodeBodyService {
    /**
     * 解析加密参数
     * @param body 原始报文数据
     * @return
     */
    public String decodeRequestBody(String body);
}
```

## 3.6 SpringBoot配置文件参数加密

### 3.6.1 使用内置PBE算法处理加密参数

**由于配置文件参数解析在Spring初始化的时候完成，因此默认启用，无法关闭。**

使用PBE解密算法样例配置参数如下：

```yaml
shanhai:
  envdecode:
    market:
      algorithm: PBE
      pebSalt: 'VjBnT0Qo8hI='  #自定义PBE解密盐
      pebPasswd: '20220111'    #自定义PBE解密密钥
```

SpringBoot配置参数加密说明：

1）所有加密参数的配置值以**envdecode::**打头，举例如下：

```yaml
app:
  version: 'envdecode::iezthxHWDp/fhXYXZSjhVw=='
```

2）PBE盐值及加密结果生成样例

```java
public static void main(String[] args) throws Exception {
    //生成盐值
    String salt= PBEUtils.initSalt();
    //自定义密钥
    String passwd="20220111";
    //待加密参数值
    String val="zhangsanxxx";
    String encryptStr=PBEUtils.encrypt(val,passwd,salt);
    //加密结果
    System.out.println(salt+":"+encryptStr);
    //解密结果
    System.out.println(PBEUtils.decrypt(encryptStr,passwd,salt));
}
```

### 3.6.2 自定义处理加密参数

首先需要实现一个自己的参数解密类，要求该类必须继承于PropertyDecode，并实现其定义的解密方法getProperty，PropertyDecoded定义如下：

```java
public  abstract class PropertyDecode {
    /**
     * 自定义解析算法
     * @param envProperties 通用配置参数 
     * @param configProperties 待解密属性key:value
     * @return
     */
    @Nullable
    public abstract Properties getProperty(Properties envProperties,Properties configProperties);
}
```

**注：SpringBoot配置文件中shanhai.envdecode打头的参数都会传递到envProperties中，可以自己获取自己配置的自定义参数。**

在SpringBoot的配置文件中指定自己新增的自定义解密类，如下所示：

```yaml
shanhai:
 envdecode:
    # 自定义解密类的优先级最高
    className: 'com.xxx.RSAPropertyDecode'
```


**注 ：如果既存在自定义解密类，又存在PBE解密配置的，自定义解密类优先级最高。**

## 3.7 常见问题

### 3.7.1 文件上传检测不生效

在springboot中使用多个继承WebMvcConfigurationSupport的类是行不通的，而且使用注解@configuration去加载配置类只能挂载一个继承WebMvcConfigurationSupport。

解决办法：自己项目中的webmvc配置要实现 webMvcConfigurer 接口而不能使用继承WebMvcConfigurationSupport类的方式，这样组件和自己项目中的webmvc都可以挂载。

示例如下：

```java
@Configuration
public class XXXConfig implements WebMvcConfigurer {

}
```
