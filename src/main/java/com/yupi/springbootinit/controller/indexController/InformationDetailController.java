package com.yupi.springbootinit.controller.indexController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.model.entity.InformationDetail;
import com.yupi.springbootinit.model.entity.SpiritMineDescription;
import com.yupi.springbootinit.model.entity.SpiritStoneUserDetail;
import com.yupi.springbootinit.model.vo.SpiritMineDescriptionVo;
import com.yupi.springbootinit.model.vo.StoenDetailVo;
import com.yupi.springbootinit.model.vo.StoneQuantityDetailVo;
import com.yupi.springbootinit.service.InformationDetailService;
import com.yupi.springbootinit.service.PriceService;
import com.yupi.springbootinit.service.SpiritMineDescriptionService;
import com.yupi.springbootinit.service.SpiritStoneUserDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 *
 */
@RestController
@RequestMapping("/informationDetail")
@Slf4j
public class InformationDetailController {
    @Resource
    private InformationDetailService informationDetailService;
    @Resource
    private SpiritStoneUserDetailService spiritStoneUserDetailService;
    @Resource
    private SpiritMineDescriptionService spiritMineDescriptionService;
    @Autowired
    private PriceService priceService;

    @GetMapping("/detail")
    public BaseResponse<List<InformationDetail>> getInformationList(@RequestParam("id") Long informationId) {
        List<InformationDetail> list = informationDetailService.getDetail(informationId);
        return ResultUtils.success(list);
    }
    @GetMapping("/save")
    public BaseResponse<Boolean> saveInformationList() {
        Boolean b = informationDetailService.addImmortal();
        return ResultUtils.success(b);
    }

    @GetMapping("/spiritStoneDeail")
    public BaseResponse<Boolean> spiritStoneList(@RequestParam("id") Long informationId) {
        spiritStoneUserDetailService.addOrUpdateDeta();
        return ResultUtils.success(true);
    }
    /**
     * 获取矿主信息（分页查询）
     * @param pageNum 页码，默认第1页
     * @param pageSize 每页数量，默认10条
     * @return 分页结果
     */
    @GetMapping("/userList")
    public BaseResponse<Page<StoenDetailVo>> userDeailList(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "15") long pageSize
    ) {
        // 构建分页对象
        Page<SpiritStoneUserDetail> page = new Page<>(pageNum, pageSize);
        // 构建查询条件（示例：根据创建时间倒序）
        QueryWrapper<SpiritStoneUserDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        // 执行分页查询（这里需要注入对应的Service）
        Page<SpiritStoneUserDetail> entityPage = spiritStoneUserDetailService.page(page, queryWrapper);
        // 2. 实体列表转 VO 列表（假设你有转换工具/手动转换逻辑）
        List<StoenDetailVo> voList = entityPage.getRecords().stream()
                .map(entity -> {
                    StoenDetailVo vo = new StoenDetailVo();
                    vo.setId(entity.getId());
                    vo.setName(entity.getName());
                    vo.setStoneTotal(String.valueOf(entity.getStoneTotal()));
                    return vo;
                })
                .collect(Collectors.toList());
        // 3. 构建 VO 的分页对象
        Page<StoenDetailVo> voPage = new Page<>();
        voPage.setRecords(voList);
        voPage.setTotal(entityPage.getTotal());   // 总条数
        voPage.setSize(entityPage.getSize());     // 每页条数
        voPage.setCurrent(entityPage.getCurrent()); // 当前页
        voPage.setPages(entityPage.getPages());   // 总页数
        // 4. 返回 VO 分页
        return ResultUtils.success(voPage);
    }

    @GetMapping("/stoneSizeDeail")
    public BaseResponse<StoneQuantityDetailVo> getstoneSizDeail() {
        StoneQuantityDetailVo date = spiritStoneUserDetailService.getDate();
        return ResultUtils.success(date);
    }
    @GetMapping("/userDetail")
    public BaseResponse<SpiritStoneUserDetail> getuserDetail(@RequestParam("id") String id) {
        ThrowUtils.throwIf(id.isEmpty(),ErrorCode.PARAMS_ERROR);
        SpiritStoneUserDetail result= spiritStoneUserDetailService.getOne(Wrappers.lambdaQuery(SpiritStoneUserDetail.class).eq(SpiritStoneUserDetail::getId, id));
        if (result!=null) {
            return ResultUtils.success(result);
        }
        return ResultUtils.error(ErrorCode.NOT_FOUND_ERROR);
    }

    @GetMapping("/profitList")
    public BaseResponse<List<SpiritMineDescriptionVo>> getProfitList() {
        List<SpiritMineDescription> list = spiritMineDescriptionService.list();
        List<SpiritMineDescriptionVo> result= list.stream().map(item -> {
            SpiritMineDescriptionVo spiritMineDescriptionVo = new SpiritMineDescriptionVo();
            spiritMineDescriptionVo.setName(item.getName());
            spiritMineDescriptionVo.setInputStone(item.getInputStone());
            spiritMineDescriptionVo.setOutputStone(item.getOutputStone());
            spiritMineDescriptionVo.setMonthlyProfit(item.getMonthlyProfit());
            return spiritMineDescriptionVo;
        }).collect(Collectors.toList());
        return ResultUtils.success(result);
    }
    @GetMapping("/addMineDescrip")
    public BaseResponse<String> addMineDescrip() {
        spiritMineDescriptionService.saveOrUpdateDetail();
        return ResultUtils.success("初始化成功");
    }
    @GetMapping("/addMineDescrip1")
    public BaseResponse<String> addMineDescrip1() {
        spiritStoneUserDetailService.addOrUpdateDeta();
        return ResultUtils.success("初始化成功");
    }

    @GetMapping("/test")
    public BaseResponse<String> test() throws JsonProcessingException {
//      priceService.getSuperCurrency();
        try {
            Thread.sleep(1000+new Random().nextInt(200)); // 线程暂停 1000毫秒（即 1 秒）
//            priceService.getAirDrop();
        } catch (InterruptedException e) {
            e.printStackTrace();
            // 当线程在睡眠时被中断，会抛出此异常
        }
//        try {
//            priceService.getAuspiciousStarTalisman();
//            Thread.sleep(1000+new Random().nextInt(200)); // 线程暂停 1000毫秒（即 1 秒）
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            // 当线程在睡眠时被中断，会抛出此异常
//        }
//        try {
//            priceService.getImmortalSpecies();
//            Thread.sleep(1000+new Random().nextInt(200)); // 线程暂停 1000毫秒（即 1 秒）
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            // 当线程在睡眠时被中断，会抛出此异常
//        }
//        try {
//            priceService.getCreationFruit();//造化果
//            Thread.sleep(1000+new Random().nextInt(200)); // 线程暂停 1000毫秒（即 1 秒）
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            // 当线程在睡眠时被中断，会抛出此异常
//        }
//        try {
//            priceService.getMeritStonePackage() ; //29功德石包
//            Thread.sleep(1000+new Random().nextInt(200)); // 线程暂停 1000毫秒（即 1 秒）
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            // 当线程在睡眠时被中断，会抛出此异常
//        }
//        try {
//            priceService.getGinseng();//30人参
//            Thread.sleep(1000+new Random().nextInt(200)); // 线程暂停 1000毫秒（即 1 秒）
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            // 当线程在睡眠时被中断，会抛出此异常
//        }
//        priceService.getRwVirtualStocks();//31仙侠宇宙虚拟股
        return ResultUtils.success("初始化成功");
    }
}