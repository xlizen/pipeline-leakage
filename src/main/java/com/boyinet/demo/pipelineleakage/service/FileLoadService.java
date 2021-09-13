package com.boyinet.demo.pipelineleakage.service;


import com.boyinet.demo.pipelineleakage.bean.primary.History;
import com.boyinet.demo.pipelineleakage.repository.primary.HistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lengchunyun
 */
@Service
public class FileLoadService {


    private final HistoryRepository historyRepository;

    public FileLoadService(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    public void loadFileToDataBase(String src) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/M/d HH:mm:ss");
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(new File("D:\\无线压力表\\30日数据整理 210518.txt")));
            String buffer;
            List<History> historyList = new ArrayList<>(1000);
            int length = 0;
            while ((buffer = bufferedReader.readLine()) != null) {
                String[] split = buffer.split("\t");
                if (split.length > 24) {
                    for (int i = 0; i < 8; i++) {
                        String val = split[i * 3 + 1];
                        if (StringUtils.hasText(val)) {
                            if (length >= 999) {
                                historyRepository.saveAll(historyList);
                                length = 0;
                                historyList.clear();
                            }
                            length++;
                            String dateStr = split[i * 3];
                            History history = new History(new BigDecimal(val), dateFormat.parse(dateStr), i + 1L);
                            historyList.add(history);
                        }
                    }
                }
            }
            if (historyList.size() != 0) {
                historyRepository.saveAll(historyList);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
