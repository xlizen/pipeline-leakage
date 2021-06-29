package com.boyinet.demo.pipelineleakage.controller;


import cn.hutool.core.date.DateUtil;
import com.boyinet.demo.pipelineleakage.common.AppFileUtils;
import com.boyinet.demo.pipelineleakage.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 文件上传
 *
 * @author LJH
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RestController
@RequestMapping("file")
@Slf4j
public class FileController {

    /**
     * 文件上传
     */
    @RequestMapping("uploadFile")
    public R<Map<String, Object>> uploadFile(MultipartFile file) {
        Map<String, Object> map = new HashMap<>(6);
        //1,得到文件名
        String oldName = file.getOriginalFilename();
        //2,根据文件名生成新的文件名
        String newName = AppFileUtils.createNewFileName(Objects.requireNonNull(oldName));
        //3,得到当前日期的字符串
        String dirName = DateUtil.format(new Date(), "yyyy-MM-dd");
        //4,构造文件夹
        File dirFile = new File(AppFileUtils.UPLOAD_PATH, dirName);
        //5,判断当前文件夹是否存在
        if (!dirFile.exists()) {
            dirFile.mkdirs();//创建文件夹
        }
        //6,构造文件对象
        File temp = new File(dirFile, newName + "_temp");
        //7,把mf里面的图片信息写入file
        try {
            file.transferTo(temp);
            BufferedImage image = ImageIO.read(temp);
            if (image != null && image.getWidth() > 0 && image.getHeight() > 0) {
                map.put("extent", new Integer[]{0, 0, image.getWidth(), image.getHeight()});
            }
        } catch (IllegalStateException | IOException e) {
            log.warn("error:{}", e.getMessage(), e);
        }
        map.put("title", dirName + "/" + newName + "_temp");
        map.put("src", "/file/showImageByPath?path=" + dirName + "/" + newName + "_temp");
        return R.ok(map);
    }


    /**
     * 图片下载
     *
     * @param path 图片的路径
     */
    @RequestMapping("showImageByPath")
    public ResponseEntity<Object> showImageByPath(String path) {
        return AppFileUtils.createResponseEntity(path);
    }

    /**
     * 文件下载
     *
     * @param path 文件的路径
     */
    @RequestMapping("download")
    public ResponseEntity<Object> download(String path, @RequestParam(required = false) String fileName) {
        return AppFileUtils.fileResponseEntity(path, fileName);
    }

    /**
     * 音乐下载
     *
     * @param path 音频文件的路径
     */
    @RequestMapping("play")
    public void play(String path, HttpServletResponse response, HttpServletRequest request) {
        AppFileUtils.musicResponseEntity(path, response, request);
    }
}
