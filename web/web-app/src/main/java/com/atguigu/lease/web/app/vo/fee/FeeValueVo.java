package com.atguigu.lease.web.app.vo.fee;

import com.atguigu.lease.model.entity.FeeValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "杂费值")
@Data
public class FeeValueVo extends FeeValue implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "费用所对的fee_key名称")
    private String feeKeyName;
}
