package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.dto.Market.InformationDto;
import com.yupi.springbootinit.model.entity.Information;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface InformationService extends IService<Information> {

    Boolean saveInformation(InformationDto informationDto, MultipartFile photo) throws IOException;
}
