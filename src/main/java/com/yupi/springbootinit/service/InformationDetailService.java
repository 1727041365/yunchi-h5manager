package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.yupi.springbootinit.model.entity.InformationDetail;

import java.util.List;

public interface InformationDetailService extends IService<InformationDetail> {
    List<InformationDetail> getDetail(Long informationId);

    Boolean updateImmortal();

    //13. 获取仙的平均战力,日采矿数量,合成成功率
    Boolean addImmortal();

    void getOreDetail() throws JsonProcessingException;
}
