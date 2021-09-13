package com.boyinet.demo.pipelineleakage.repository.primary;

import com.boyinet.demo.pipelineleakage.bean.primary.Zero;
import com.boyinet.demo.pipelineleakage.bean.primary.ZeroKey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author lengchunyun
 */
@Repository
public interface ZeroRepository extends JpaRepository<Zero, ZeroKey>, JpaSpecificationExecutor<Zero> {


    /**
     * 分页获取零点数据
     *
     * @param no   起始no
     * @param page 分页信息
     * @return 结果
     */
    default Page<Zero> listZero(Long no, Pageable page) {
        return this.findAll((Specification<Zero>) (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.ge(root.get("no").as(Long.class), no), page);
    }

    List<Zero> findZerosByNoIn(List<Long> ids);
}
