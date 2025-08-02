package com.yupi.springbootinit.controller.indexController;

import cn.hutool.core.bean.BeanUtil;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.model.dto.Market.InformationDto;
import com.yupi.springbootinit.model.entity.Information;
import com.yupi.springbootinit.model.vo.InformationVo;
import com.yupi.springbootinit.service.InformationService;
import com.yupi.springbootinit.utils.SavePhotoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/information")
public class InformationController {
    @Resource
    private InformationService informationService;

    @GetMapping("/list")
    public BaseResponse<List<InformationVo>> getInformationList() {
        List<Information> list = informationService.list();
        log.info("list={}", list);
        list.sort(Comparator.comparing(Information::getCreateTime).reversed());
        if (list != null) {
            List<InformationVo> result = list.stream().map(item -> {
                InformationVo informationVo = new InformationVo();
                BeanUtils.copyProperties(item, informationVo);
                return informationVo;
            }).collect(Collectors.toList());
            return ResultUtils.success(result);
        } else {
            return ResultUtils.success(null);
        }
    }

    @PostMapping("/add")
    public BaseResponse<String> addInformation(@RequestParam String title,@RequestParam String content, @RequestPart MultipartFile photo) throws IOException {
        ThrowUtils.throwIf(photo.isEmpty(), ErrorCode.PARAMS_ERROR, "图片未上传");
        InformationDto informationDto = new InformationDto(title,content);
        ThrowUtils.throwIf(BeanUtil.isEmpty(informationDto), ErrorCode.PARAMS_ERROR, "缺少标题或内容");
        Boolean result = informationService.saveInformation(informationDto, photo);
        if (result) {
            return ResultUtils.success("成功添加至首页");
        } else {
            return ResultUtils.error(ErrorCode.OPERATION_ERROR);
        }
    }

    @PostMapping("/update")
    public BaseResponse<String> updateInformation(@RequestParam Information information, @RequestPart MultipartFile photo) throws IOException {
        ThrowUtils.throwIf( BeanUtil.isEmpty(information.getId()),ErrorCode.PARAMS_ERROR,"上传数据为空");
        if (photo.isEmpty()) {
            boolean result = informationService.updateById(information);
            if (result) {
                return ResultUtils.success("成功更新");
            } else {
                return ResultUtils.error(ErrorCode.OPERATION_ERROR);
            }
        }else {
            String path = SavePhotoUtil.saveHInformationPhoto(photo);
            information.setPhotoPath(path);
            boolean result = informationService.updateById(information);
            if (result) {
                return ResultUtils.success("成功更新");
            } else {
                return ResultUtils.error(ErrorCode.OPERATION_ERROR);
            }
        }
    }
}
