package com.boyinet.demo.pipelineleakage.common;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.Properties;

/**
 * 文件上传下载工具类
 *
 * @author LJH
 */
@Slf4j
public class AppFileUtils {

    private static final String RANGE_HEARD = "bytes=";
    private static final String RANGE_SPLITTER = "-";
    /**
     * 文件上传的保存路径
     */
    public static String UPLOAD_PATH = "D:/upload/";

    static {
        //读取配置文件的存储地址
        InputStream stream = AppFileUtils.class.getClassLoader().getResourceAsStream("file.properties");
        Properties properties = new Properties();
        try {
            properties.load(stream);
        } catch (IOException e) {
            log.warn("error:{}", e.getMessage(), e);
        }
        String property = properties.getProperty("filepath");
        if (null != property) {
            UPLOAD_PATH = property;
        }
    }

    /**
     * 根据文件老名字得到新名字
     *
     * @param oldName 原始名称
     * @return 返回新名称
     */
    public static String createNewFileName(String oldName) {
        String stuff = oldName.substring(oldName.lastIndexOf('.'));
        return IdUtil.simpleUUID().toUpperCase() + stuff;
    }

    /**
     * 文件下载
     *
     * @param path 文件路径
     * @return ResponseEntity对象
     */
    public static ResponseEntity<Object> createResponseEntity(String path) {
        //1,构造文件对象
        File file = new File(UPLOAD_PATH, path);
        if (file.exists()) {
            //将下载的文件，封装byte[]
            byte[] bytes = null;
            try {
                bytes = FileUtil.readBytes(file);
            } catch (Exception e) {
                log.warn("error:{}", e.getMessage(), e);
            }

            //创建封装响应头信息的对象
            HttpHeaders header = new HttpHeaders();
            //封装响应内容类型(APPLICATION_OCTET_STREAM 响应的内容不限定)
            header.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            //设置下载的文件的名称
            //创建ResponseEntity对象
            ResponseEntity<Object> entity;
            entity = new ResponseEntity<>(bytes, header, HttpStatus.CREATED);
            return entity;
        }
        return null;
    }

    /**
     * 文件下载
     *
     * @param path 文件路径
     * @return ResponseEntity对象
     */
    public static ResponseEntity<Object> fileResponseEntity(String path, String fileName) {
        //1,构造文件对象
        File file = new File(UPLOAD_PATH, path);
        if (file.exists()) {
            //将下载的文件，封装byte[]
            byte[] bytes = null;
            try {
                bytes = FileUtil.readBytes(file);
            } catch (Exception e) {
                log.warn("error:{}", e.getMessage(), e);
            }

            //创建封装响应头信息的对象
            HttpHeaders header = new HttpHeaders();
            //设置下载的文件的名称
            if (StrUtil.isBlank(fileName)) {
                fileName = file.getName();
            }
            try {
                header.add("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
                header.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                //创建ResponseEntity对象
                ResponseEntity<Object> entity;
                entity = new ResponseEntity<>(bytes, header, HttpStatus.OK);
                return entity;
            } catch (UnsupportedEncodingException e) {
                log.warn("error:{}", e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * 根据路径改名字 去掉_temp
     *
     * @param img 名称
     * @return 新名称
     */
    public static String renameFile(String img) {
        File file = new File(UPLOAD_PATH, img);
        String replace = img.replace("_temp", "");
        if (file.exists() && file.renameTo(new File(UPLOAD_PATH, replace))) {
            return replace;
        }
        return "";
    }

    /**
     * 根据老路径删除图片
     *
     * @param oldPath 历史路径
     */
    public static void removeFileByPath(String oldPath) {
        File file = new File(UPLOAD_PATH, oldPath);
        if (file.delete()) {
            return;
        }
        log.warn(oldPath + "删除失败");
    }

    public static void musicResponseEntity(String path, HttpServletResponse response, HttpServletRequest request) {
        // 文件目录（偷懒没有改变量，名称，此处和FTP没关系，就是文件的绝对路径）
        // 也就是 File music = new File("C:\Users\Administrator\AppData\Local\Temp\a.mp3");
        File music = new File(UPLOAD_PATH + path);
        if (!music.exists()) {
            System.out.println(UPLOAD_PATH + "不存在 文件名为：" + path + "的音频文件！");
        }
        String range = request.getHeader("Range");
        //开始下载位置
        long startByte = 0;
        //结束下载位置
        long endByte = music.length() - 1;
        //有range的话
        if (range != null && range.contains(RANGE_HEARD) && range.contains(RANGE_SPLITTER)) {
            range = range.substring(range.lastIndexOf('=') + 1).trim();
            String[] ranges = range.split(RANGE_SPLITTER);
            try {
                //判断range的类型
                if (ranges.length == 1) {
                    //类型一：bytes=-2343
                    if (range.startsWith(RANGE_SPLITTER)) {
                        endByte = Long.parseLong(ranges[0]);
                    }
                    //类型二：bytes=2343-
                    else if (range.endsWith(RANGE_SPLITTER)) {
                        startByte = Long.parseLong(ranges[0]);
                    }
                }
                //类型三：bytes=22-2343
                else if (ranges.length == 2) {
                    startByte = Long.parseLong(ranges[0]);
                    endByte = Long.parseLong(ranges[1]);
                }
            } catch (NumberFormatException e) {
                startByte = 0;
                endByte = music.length() - 1;
            }
        }
        //要下载的长度
        long contentLength = endByte - startByte + 1;
        //文件名
        String fileName = music.getName();
        //文件类型
        String contentType = request.getServletContext().getMimeType(fileName);
        //各种响应头设置
        //参考资料：https://www.ibm.com/developerworks/cn/java/joy-down/index.html
        //坑爹地方一：看代码
        response.setHeader("Accept-Ranges", "bytes");
        //坑爹地方二：http状态码要为206
        response.setStatus(206);
        response.setHeader("Content-Type", contentType);
        //不缓存
        response.setHeader("Cache-Control", "no-store");
        //这里文件名换你想要的，inline表示浏览器直接实用（我方便测试用的）
        //参考资料：http://hw1287789687.iteye.com/blog/2188500
        //response.setHeader("Content-Disposition", "inline;filename=test.mp3");
        response.setHeader("Content-Length", String.valueOf(contentLength));
        //坑爹地方三：Content-Range，格式为
        // [要下载的开始位置]-[结束位置]/[文件总大小]
        response.setHeader("Content-Range", "bytes " + startByte + "-" + endByte + "/" + music.length());
        BufferedOutputStream outputStream;
        RandomAccessFile randomAccessFile = null;
        //已传送数据大小
        long transmitted = 0;
        try {
            randomAccessFile = new RandomAccessFile(music, "r");
            outputStream = new BufferedOutputStream(response.getOutputStream());
            byte[] buff = new byte[4096];
            int len = 0;
            randomAccessFile.seek(startByte);
            //坑爹地方四：判断是否到了最后不足4096（buff的length）个byte这个逻辑（(transmitted + len) <= contentLength）要放前面！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
            //不然会会先读取randomAccessFile，造成后面读取位置出错，找了一天才发现问题所在
            while ((transmitted + len) <= contentLength && (len = randomAccessFile.read(buff)) != -1) {
                outputStream.write(buff, 0, len);
                transmitted += len;
            }
            //处理不足buff.length部分
            if (transmitted < contentLength) {
                len = randomAccessFile.read(buff, 0, (int) (contentLength - transmitted));
                outputStream.write(buff, 0, len);
                transmitted += len;
            }
            outputStream.flush();
            response.flushBuffer();
            randomAccessFile.close();
            log.info("下载完毕：{}-{}: {}", startByte, endByte, transmitted);
        } catch (ClientAbortException e) {
            log.info("用户停止下载：{}-{}: {}", startByte, endByte, transmitted);
            //捕获此异常表示拥护停止下载
        } catch (IOException e) {
            log.warn("error:{}", e.getMessage(), e);
        } finally {
            try {
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (IOException e) {
                log.warn("error:{}", e.getMessage(), e);
            }
        }
    }
}
