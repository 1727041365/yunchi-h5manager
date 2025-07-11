package com.yupi.springbootinit.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.model.entity.Hot;
import com.yupi.springbootinit.service.HotService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/hot")
public class HotController {

    @Resource
    private HotService hotService;

    // 新增热点
    @PostMapping("/add")
    public BaseResponse<Long> addHot(@RequestBody Hot hot) {
        if (hot == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 校验数据
        String title = hot.getTitle();
        if (StringUtils.isBlank(title)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题不能为空");
        }
        
        boolean result = hotService.save(hot);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return ResultUtils.success(hot.getId());
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