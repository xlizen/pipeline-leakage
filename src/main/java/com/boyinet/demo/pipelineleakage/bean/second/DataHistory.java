package com.boyinet.demo.pipelineleakage.bean.second;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author lengchunyun
 * @since 2020-03-17
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class DataHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id主键
     */
    @Id()
    private Long id;

    /**
     * 数据点id
     */
    private String pointId;

    /**
     * 设备id
     */
    private String equipmentId;

    /**
     * 从机id
     */
    private String slaveId;

    /**
     * 数据id，便于快速查询
     */
    private String dataId;

    /**
     * 数据值
     */
    private String dataValue;

    /**
     * 数据单位
     */
    private String unit;


    /**
     * 创建时间
     */
    private Long gmtCreate;


}
