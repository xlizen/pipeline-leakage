package com.boyinet.demo.pipelineleakage.bean.primary;


import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.boyinet.demo.pipelineleakage.util.LongJsonDeserializer;
import com.boyinet.demo.pipelineleakage.util.LongJsonSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author lengchunyun
 */
@Data
@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Sensor {

    /**
     * 序列号
     */
    @Id()
    @JsonSerialize(using = LongJsonSerializer.class)
    @JsonDeserialize(using = LongJsonDeserializer.class)
    private Long no;

    /**
     * 当前值
     */
    @ExcelIgnore
    private BigDecimal currentValue;

    /**
     * 差压值
     */
    @ExcelIgnore
    private BigDecimal diffValue;

    /**
     * 均值
     */
    @ExcelIgnore
    private BigDecimal averageValue;

    /**
     * 系数k
     */
    @ExcelIgnore
    private BigDecimal k;

    /**
     * 前置传感器NO
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    @JsonDeserialize(using = LongJsonDeserializer.class)
    private Long pre;

    /**
     * 后置传感器NO
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    @JsonDeserialize(using = LongJsonDeserializer.class)
    private Long next;

    /**
     * 管路半径 ，每段都需指定
     */
    private BigDecimal r;

    /**
     * 两个传感器间距
     */
    private BigDecimal l;

    /**
     * 管路半径 ，计算值
     */
    @ExcelIgnore
    private BigDecimal calcR;

    /**
     * 管路距首支距离 ，计算值
     */
    @ExcelIgnore
    private BigDecimal calcL;

    /**
     * 0 为支路首支 1 为普通 3 为变径转角 4 为管路首支
     */
    private Byte type;

    /**
     * 管路头
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    @JsonDeserialize(using = LongJsonDeserializer.class)
    private Long head;

    /**
     * 管路ID
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    @JsonDeserialize(using = LongJsonDeserializer.class)
    @ExcelIgnore
    private Long pipelineId;

    /**
     * 创建时间
     */
    @ExcelIgnore
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(value = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

}
