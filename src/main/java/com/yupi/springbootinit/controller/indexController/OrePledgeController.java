package com.yupi.springbootinit.controller.indexController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.model.vo.OrePledgeBreakDownVo;
import com.yupi.springbootinit.model.vo.OrePledgeVo;
import com.yupi.springbootinit.service.OrePledgeBreakDownService;
import com.yupi.springbootinit.service.OrePledgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/orePledge")
@Slf4j
public class OrePledgeController {
    @Resource
    private OrePledgeService orePledgeService;
    @Resource
    private OrePledgeBreakDownService orePledgeBreakDownService;
    /**
     * 获取页面展示数据
     */
    @GetMapping("get")
    public BaseResponse<OrePledgeVo> getOrePledge() {
        OrePledgeVo orePledgeVo = orePledgeService.getDate();
        if (orePledgeVo == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        } else {
            return ResultUtils.success(orePledgeVo);
        }
    }
    @PostMapping("/page")
    public Page<OrePledgeBreakDownVo> pageQuery(@RequestParam(defaultValue = "1") int pageNum,
                                                @RequestParam(defaultValue = "10") int pageSize) {
        return orePledgeBreakDownService.pageQuery(pageNum, pageSize);
    }

}
