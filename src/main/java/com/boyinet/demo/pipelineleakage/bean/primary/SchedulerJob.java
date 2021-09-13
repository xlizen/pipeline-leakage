package com.boyinet.demo.pipelineleakage.bean.primary;

import com.boyinet.demo.pipelineleakage.util.LongJsonDeserializer;
import com.boyinet.demo.pipelineleakage.util.LongJsonSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchedulerJob {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonSerialize(using = LongJsonSerializer.class)
    @JsonDeserialize(using = LongJsonDeserializer.class)
    private Long id;

    private String group;

    private String name;

    private String content;

    private Integer interval;

    private Byte runType;

    private Byte status;

    private Long pipeLineId;


}
