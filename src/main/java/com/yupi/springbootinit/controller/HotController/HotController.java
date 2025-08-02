package com.yupi.springbootinit.controller.HotController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.model.entity.Hot;
import com.yupi.springbootinit.service.HotService;
import com.yupi.springbootinit.utils.SavePhotoUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/hot")
public class HotController {

    @Resource
    private HotService hotService;
    @PostMapping("/add")
    public BaseResponse<Long> addHot(@RequestParam("tittle") String tittle,
                                     @RequestParam("content") String content
            ,@RequestPart("photo") MultipartFile photo) {
        if (tittle == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (content == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        try {
            // 保存图片到文件系统
            String photoPath = SavePhotoUtil.saveHotPhoto(photo);
            // 创建实体
            Hot hot = new Hot();
            hot.setTitle(tittle);
            hot.setContent(content);
            hot.setPhotoPath(photoPath);
            boolean result = hotService.save(hot);
            if (!result) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR);
            }
            return ResultUtils.success(hot.getId());
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片保存失败");
        }
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteHot(@RequestBody Long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = hotService.removeById(id);
        return ResultUtils.success(result);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateHot(@RequestBody Hot hot) {
        if (hot == null || hot.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = hotService.updateById(hot);
        return ResultUtils.success(result);
    }

    // 根据ID获取热点
    @GetMapping("/get")
    public BaseResponse<Hot> getHotById(@RequestParam Long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Hot hot = hotService.getById(id);
        if (hot == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(hot);
    }

    // 获取热点列表
    @GetMapping("/list")
    public BaseResponse<List<Hot>> listHots(@RequestParam(required = false) String title) {
        QueryWrapper<Hot> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(title)) {
            queryWrapper.like("title", title);
        }
        // 按创建时间降序排列
        queryWrapper.orderByDesc("create_time");
        List<Hot> hotList = hotService.list(queryWrapper);
        System.out.println(hotList);
        return ResultUtils.success(hotList);
    }

    // 分页获取热点列表
    @GetMapping("/list/page")
    public BaseResponse<Page<Hot>> listHotsByPage(@RequestParam(required = false) String title,
                                                  @RequestParam(defaultValue = "1") long pageNum,
                                                  @RequestParam(defaultValue = "10") long pageSize) {
        QueryWrapper<Hot> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(title)) {
            queryWrapper.like("title", title);
        }
        queryWrapper.orderByDesc("create_time");
        Page<Hot> page = new Page<>(pageNum, pageSize);
        Page<Hot> resultPage = hotService.page(page, queryWrapper);
        return ResultUtils.success(resultPage);
    }
}