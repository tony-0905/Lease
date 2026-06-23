package com.atguigu.lease.web.app.vo.appointment;

import com.atguigu.lease.model.entity.ViewAppointment;
import com.atguigu.lease.web.app.vo.apartment.ApartmentItemVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;


@Data
@Schema(description = "APP端预约看房详情")
public class AppointmentDetailVo extends ViewAppointment implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "公寓基本信息")
    private ApartmentItemVo apartmentItemVo;
}
