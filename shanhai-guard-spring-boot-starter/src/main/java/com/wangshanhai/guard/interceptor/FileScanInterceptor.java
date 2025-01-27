package com.wangshanhai.guard.interceptor;

import com.wangshanhai.guard.annotation.FileGuard;
import com.wangshanhai.guard.annotation.FileType;
import com.wangshanhai.guard.config.FileGuardConfig;
import com.wangshanhai.guard.utils.HttpBizException;
import com.wangshanhai.guard.utils.Logger;
import org.apache.commons.io.IOUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 文件扫描拦截器
 */
public class FileScanInterceptor extends HandlerInterceptorAdapter {
    /**
     * 配置参数
     */
    private FileGuardConfig fileGuardConfig;

    public FileScanInterceptor(FileGuardConfig fileGuardConfig) {
        this.fileGuardConfig = fileGuardConfig;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        MultipartResolver res=new StandardServletMultipartResolver();
        FileGuard fileGuard=null;
        if(handler instanceof HandlerMethod){
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            fileGuard=handlerMethod.getMethodAnnotation(FileGuard.class);
        }
        if(res.isMultipart(request)){
            MultipartHttpServletRequest multipartRequest=(MultipartHttpServletRequest) request;
            Map<String, MultipartFile> files= multipartRequest.getFileMap();
            Iterator<String> iterator = files.keySet().iterator();
            while (iterator.hasNext()) {
                String formKey =iterator.next();
                MultipartFile multipartFile = multipartRequest.getFile(formKey);
                if (!StringUtils.isEmpty(multipartFile.getOriginalFilename())) {
                    String filename = multipartFile.getOriginalFilename();
                    if(checkFile(filename)){
                        if(fileGuardConfig.getLogTarce()){
                            Logger.info("[file-upload-log]-url:{},file:{}",request.getRequestURI(),filename);
                        }
                        if(fileGuard!=null){
                            return checkGuard(multipartFile,fileGuard);
                        }
                    }else{
                        Logger.error("[file-upload-alert]-url:{},file:{}",request.getRequestURI(),filename);
                        throw new HttpBizException("不允许上传该类型文件！");
                    }
                }
            }
        }
        return true;
    }

    /**
     * 单个方法校验
     * @param file 文件
     * @return
     */
    private  boolean checkGuard(MultipartFile file, FileGuard fileGuard){
         String[] suffixes = fileGuard.supportedSuffixes();
         FileGuard.GuardType type = fileGuard.type();
         FileType[] fileTypes = fileGuard.supportedFileTypes();
         if (suffixes.length==0 && fileTypes.length==0) {
            return true;
         }
         Set<String> suffixSet = new HashSet<>(Arrays.asList(suffixes));
         for (FileType fileType : fileTypes) {
            suffixSet.add(fileType.getSuffix());
         }
         Set<FileType> fileTypeSet = new HashSet<>(Arrays.asList(fileTypes));
         for (String suffix : suffixes) {
            fileTypeSet.add(FileType.getBySuffix(suffix));
         }
        if (type ==  FileGuard.GuardType.SUFFIX) {
            return suffixCheck(file, suffixSet, fileGuard.message());
        } else {
           return  bytesCheck(file, fileTypeSet, fileGuard.message());
        }
    }

    /**
     * 通过文件头方式校验
     * @param file 文件
     * @param fileTypeSet 文件头白名单
     * @param message 异常提示
     * @return
     */
    private boolean bytesCheck(MultipartFile file, Set<FileType> fileTypeSet, String message) {
        String hexNumber = readFileHeader(file);
        for (FileType fileType : fileTypeSet) {
            if (!StringUtils.isEmpty(hexNumber)&&hexNumber.startsWith(fileType.getHexNumber())) {
                return true;
            }
        }
        throw new HttpBizException(message);
    }

    /**
     * 通过文件后缀校验
     * @param file 文件
     * @param suffixSet 文件后缀白名单
     * @param message 异常提示
     * @return
     */
    private boolean suffixCheck(MultipartFile file, Set<String> suffixSet, String message) {
        String fileName = file.getOriginalFilename();
        String fileSuffix=fileName.substring(fileName.lastIndexOf(".")+1);
        for (String suffix : suffixSet) {
            if (suffix.toUpperCase().equalsIgnoreCase(fileSuffix)) {
                return true;
            }
        }
        throw new HttpBizException(message);
    }

    /**
     * 读取文件头
     * @param file 文件
     * @return
     */
    private String readFileHeader(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            byte[] fileHeader = new byte[8];
            is.read(fileHeader);
            return byteArray2Hex(fileHeader);
        } catch (IOException e) {
            throw new HttpBizException("文件识别异常!");
        } finally {
            IOUtils.closeQuietly();
        }
    }

    /**
     * 字节数组转16进制字符串
     * @param data 文件头数据
     * @return
     */
    private String byteArray2Hex(byte[] data) {
        StringBuilder stringBuilder = new StringBuilder();
        if (data.length==0) {
            return null;
        }
        for (byte datum : data) {
            int v = datum & 0xFF;
            String hv = Integer.toHexString(v).toUpperCase();
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        String result = stringBuilder.toString();
        return result;
    }
    /**
     * 全局性校验
     * @param fileName 文件名
     * @return
     */
    private  boolean checkFile(String fileName){
        //获取文件后缀
        String suffix=fileName.substring(fileName.lastIndexOf(".")+1);
        if(fileGuardConfig.getSuffix().contains(suffix.trim().toLowerCase())){
            return true;
        }
        return false;
    }
}
