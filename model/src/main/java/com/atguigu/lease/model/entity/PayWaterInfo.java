package com.atguigu.lease.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.spi.LocaleNameProvider;

@Data
@TableName("water_fee_payment")
@AllArgsConstructor
@NoArgsConstructor
public class PayWaterInfo {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("username")
    private String username;

    @TableField("apartment_id")
    private Long apartmentId;

    @TableField("room_id")
    private Long roomId;

    @TableField("amount")
    private Long amount;

    @TableField("status")
    private Integer status;

    @TableField("pay_time")
    private LocalDateTime payTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
