package com.boyinet.demo.pipelineleakage.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.StrUtil;
import com.boyinet.demo.pipelineleakage.bean.primary.PipeLine;
import com.boyinet.demo.pipelineleakage.bean.primary.Sensor;
import com.boyinet.demo.pipelineleakage.common.AppFileUtils;
import com.boyinet.demo.pipelineleakage.common.JsonUtils;
import com.boyinet.demo.pipelineleakage.common.Tree;
import com.boyinet.demo.pipelineleakage.repository.primary.PipeLineRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lengchunyun
 */
@Service
@Slf4j
public class PipeLineService {

    private final PipeLineRepository pipeLineRepository;
    private final SensorService sensorService;

    public PipeLineService(PipeLineRepository pipeLineRepository, SensorService sensorService) {
        this.pipeLineRepository = pipeLineRepository;
        this.sensorService = sensorService;
    }

    public void save(PipeLine pipeLine) {
        String renameFile = AppFileUtils.renameFile(pipeLine.getMapFileName());
        pipeLine.setMapFileName(renameFile);
        renameFile = AppFileUtils.renameFile(pipeLine.getXlsFileName());
        pipeLine.setXlsFileName(AppFileUtils.UPLOAD_PATH + renameFile);
        PipeLine save = pipeLineRepository.save(pipeLine);
        log.info("[XLSX 文件]，文件路径{}", pipeLine.getXlsFileName());
        sensorService.readFileAndSave(AppFileUtils.UPLOAD_PATH + renameFile, save.getId());
    }

    public Page<PipeLine> list(Integer pi, Integer ps, String searchParams) {
        if (StrUtil.isNotBlank(searchParams)) {
            PipeLine pipeLine = JsonUtils.parse(searchParams, PipeLine.class);
            return pipeLineRepository.findAll(new Specification<PipeLine>() {
                @Override
                public Predicate toPredicate(Root<PipeLine> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> list = new ArrayList<>();
                    if (StrUtil.isNotBlank(pipeLine.getName())) {
                        list.add(criteriaBuilder.like(root.get("name").as(String.class), "%" + pipeLine.getName() + "%"));
                    }
                    if (null != pipeLine.getStandard()) {
                        list.add(criteriaBuilder.equal(root.get("standard").as(Double.class), pipeLine.getStandard()));
                    }
                    if (null != pipeLine.getDuration()) {
                        list.add(criteriaBuilder.equal(root.get("duration").as(Double.class), pipeLine.getDuration()));
                    }
                    return criteriaBuilder.and(list.toArray(new Predicate[]{}));
                }
            }, PageRequest.of(pi - 1, ps));
        } else {
            return pipeLineRepository.findAll(null, PageRequest.of(pi - 1, ps));
        }
    }

    public void deleteById(Long id) {
        pipeLineRepository.deleteById(id);
    }

    public void updateById(PipeLine pipeLine) {
        pipeLineRepository.findById(pipeLine.getId()).ifPresent(old -> {
            BeanUtil.copyProperties(pipeLine, old, CopyOptions.create(PipeLine.class, true));
            pipeLineRepository.save(old);
        });

    }

    public List<Tree> tree() {
        List<Tree> trees = new ArrayList<>();
        Iterable<PipeLine> all = pipeLineRepository.findAll();
        all.forEach(pipeLine -> {
            trees.add(new Tree(pipeLine.getName(), pipeLine.getId(), true));
        });
        return trees;
    }

    public PipeLine load(Long id) {
        PipeLine pipeLine = pipeLineRepository.findById(id).orElse(null);
        if (pipeLine == null) {
            return null;
        }
        List<Sensor> sensorList = sensorService.findByPid(id);
        pipeLine.setSensorList(sensorList);
        return pipeLine;
    }

    public PipeLine findById(Long pipelineId) {
        return pipeLineRepository.findById(pipelineId).orElse(null);
    }

    public List<PipeLine> findAll() {
        List<PipeLine> pipeLines = new ArrayList<>();
        pipeLineRepository.findAll().forEach(pipeLines::add);
        return pipeLines;
    }
}
