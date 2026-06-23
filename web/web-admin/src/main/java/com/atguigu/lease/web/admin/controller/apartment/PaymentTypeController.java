package com.atguigu.lease.web.admin.controller.apartment;


import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.model.entity.PaymentType;
import com.atguigu.lease.web.admin.service.PaymentTypeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "支付方式管理")
@RequestMapping("/admin/payment")
@RestController
public class PaymentTypeController {
    @Autowired
    private PaymentTypeService paymentTypeService;

    @Operation(summary = "查询全部支付方式列表")
    @GetMapping("list")
    public Result< List<PaymentType>> listPaymentType() {

        //增加过滤条件，把逻辑删除的isdelete 字段不返回给前端
//        LambdaQueryWrapper<PaymentType> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(PaymentType::getIsDeleted, 0);
        List<PaymentType> list = paymentTypeService.list();

        return Result.ok(list);
    }

    @Operation(summary = "保存或更新支付方式")
    @PostMapping("saveOrUpdate")
    public Result saveOrUpdatePaymentType(@RequestBody PaymentType paymentType) {

        paymentTypeService.saveOrUpdate(paymentType);
//        if( !result)
        return Result.ok();
    }

    @Operation(summary = "根据ID删除支付方式")
    @DeleteMapping("deleteById")
    public Result deletePaymentById(@RequestParam Long id) {
//        PaymentType list = paymentTypeService.getById(id);
        paymentTypeService.removeById(id);


        return Result.ok();
    }

}















