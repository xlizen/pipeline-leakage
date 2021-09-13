package com.boyinet.demo.pipelineleakage.bean.primary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

@Data
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ZeroKey implements Serializable {

    @Id
    private Long no;

    @Id
    private Long target;

}
