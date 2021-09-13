package com.boyinet.demo.pipelineleakage.repository.primary;

import cn.hutool.core.util.StrUtil;
import com.boyinet.demo.pipelineleakage.bean.primary.Flow;
import com.boyinet.demo.pipelineleakage.vo.FlowVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public interface FlowRepository extends CrudRepository<Flow, Long>, JpaSpecificationExecutor<Flow> {


    default Page<Flow> list(FlowVO vo) {
        return this.findAll((Specification<Flow>) (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StrUtil.isNotBlank(vo.getPipelineId())) {
                predicates.add(criteriaBuilder.equal(root.get("pipelineId").as(Long.class), vo.getPipelineId()));
            } else {
                return criteriaBuilder.and(predicates.toArray(new Predicate[]{}));
            }
            if (Objects.nonNull(vo.getSensorId())) {
                predicates.add(criteriaBuilder.equal(root.get("no").as(Long.class), vo.getSensorId()));
            }
            predicates.add(criteriaBuilder.between(root.get("time").as(Date.class), vo.getStartTime(), vo.getEndTime()));
            return criteriaBuilder.and(predicates.toArray(new Predicate[]{}));
        }, PageRequest.of(vo.getPage() - 1, vo.getLimit()));
    }
}
