package com.boyinet.demo.pipelineleakage.bean.primary;

import com.boyinet.demo.pipelineleakage.util.LongJsonDeserializer;
import com.boyinet.demo.pipelineleakage.util.LongJsonSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

/**
 * @author lengchunyun
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class PipeLine {

    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonSerialize(using = LongJsonSerializer.class)
    @JsonDeserialize(using = LongJsonDeserializer.class)
    private Long id;
    @Transient
    private List<Sensor> sensorList;
    private String xlsFileName;
    private String name;
    private Double standard;
    private Double duration;
    private String mapFileName;
    private String size;
    private String referencePoint;

    public PipeLine(Long id, String xlsFileName, String name, Double standard, Double duration,
                    String mapFileName, String size) {
        this.id = id;
        this.xlsFileName = xlsFileName;
        this.name = name;
        this.standard = standard;
        this.duration = duration;
        this.mapFileName = mapFileName;
        this.size = size;
    }
}
