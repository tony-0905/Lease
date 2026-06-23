package com.atguigu.lease.web.admin.vo.attr;

import com.atguigu.lease.model.entity.AttrValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;


@Schema(description = "属性值")
@Data
public class AttrValueVo extends AttrValue implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "对应的属性key_name")
    private String attrKeyName;
}
