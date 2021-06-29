package com.boyinet.demo.pipelineleakage.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.boyinet.demo.pipelineleakage.bean.Sensor;
import com.boyinet.demo.pipelineleakage.repository.SensorRepository;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * @author lengchunyun
 */
@Slf4j
public class UploadDataListener extends AnalysisEventListener<Sensor> {

    /**
     * 每隔500条存储数据库，实际使用中可以3000条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 500;
    List<Sensor> list = new ArrayList<>();

    private final SensorRepository sensorRepository;

    private final Integer pipeLineId;

    /**
     * 如果使用了spring,请使用这个构造方法。每次创建Listener的时候需要把spring管理的类传进来
     *
     * @param sensorRepository dao
     */
    public UploadDataListener(SensorRepository sensorRepository, Integer pipeLineId) {
        this.sensorRepository = sensorRepository;
        this.pipeLineId = pipeLineId;
    }

    /**
     * 这个每一条数据解析都会来调用
     *
     * @param data    one row value. Is is same as {@link AnalysisContext#readRowHolder()}
     * @param context 上下文
     */
    @Override
    public void invoke(Sensor data, AnalysisContext context) {
        log.info("解析到一条数据:{}", data);
        data.setPipelineId(this.pipeLineId);
        data.setCreateTime(new Date());
        list.add(data);
        // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (list.size() >= BATCH_COUNT) {
            saveData();
            // 存储完成清理 list
            list.clear();
        }
    }

    /**
     * 所有数据解析完成了 都会来调用
     *
     * @param context 上下文
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 这里也要保存数据，确保最后遗留的数据也存储到数据库
        saveData();
        log.info("所有数据解析完成！");
    }

    /**
     * 加上存储数据库
     */
    private void saveData() {
        log.info("{}条数据，开始存储数据库！", list.size());
        sensorRepository.saveAll(list);
        log.info("存储数据库成功！");
    }
}
