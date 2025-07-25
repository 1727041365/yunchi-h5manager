package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.mapper.InformationMapper;
import com.yupi.springbootinit.model.dto.Market.InformationDto;
import com.yupi.springbootinit.model.entity.Information;
import com.yupi.springbootinit.service.InformationService;
import com.yupi.springbootinit.utils.SavePhotoUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class InformationServiceImpl extends ServiceImpl<InformationMapper, Information> implements InformationService{

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveInformation(InformationDto informationDto, MultipartFile photo) throws IOException {
        String path = SavePhotoUtil.saveHInformationPhoto(photo);
        Information information = new Information();
        information.setTitle(informationDto.getTitle());
        information.setContent(informationDto.getContent());
        information.setPhotoPath(path);
        boolean save = this.save(information);
        if(save){
            return save;
        }else {
          throw  new RuntimeException();
        }
    }
}
