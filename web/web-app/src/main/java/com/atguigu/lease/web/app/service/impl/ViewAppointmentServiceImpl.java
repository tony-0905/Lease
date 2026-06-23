package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.model.entity.LabelInfo;
import com.atguigu.lease.model.entity.ViewAppointment;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.app.mapper.ApartmentInfoMapper;
import com.atguigu.lease.web.app.mapper.GraphInfoMapper;
import com.atguigu.lease.web.app.mapper.LabelInfoMapper;
import com.atguigu.lease.web.app.mapper.ViewAppointmentMapper;
import com.atguigu.lease.web.app.service.ApartmentInfoService;
import com.atguigu.lease.web.app.service.ViewAppointmentService;
import com.atguigu.lease.web.app.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.app.vo.appointment.AppointmentDetailVo;
import com.atguigu.lease.web.app.vo.appointment.AppointmentItemVo;
import com.atguigu.lease.web.app.vo.graph.GraphVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author liubo
 * @description 针对表【view_appointment(预约看房信息表)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
@Slf4j
public class ViewAppointmentServiceImpl extends ServiceImpl<ViewAppointmentMapper, ViewAppointment>
        implements ViewAppointmentService {

    @Autowired
    private ViewAppointmentMapper viewAppointmentMapper;
    @Autowired
    private ApartmentInfoService apartmentInfoService;
    @Autowired
    private RedisTemplate<String, Object> redis;



    @Override
    public List<AppointmentItemVo> listItem(Long id) {
        return viewAppointmentMapper.listItemByUserId(id);
    }

    @Override
    public AppointmentDetailVo getDetailById(Long id) {
        String key = RedisConstant.APP_APPOINTMENT_PREFIX + id;
        AppointmentDetailVo detailVo = (AppointmentDetailVo) redis.opsForValue().get(key);
        if(detailVo == null){
            log.info("redis缓存未查询到:{}", id);
            ViewAppointment viewAppointment = viewAppointmentMapper.selectById(id);
            if (viewAppointment == null) {
                return null;
            }

            ApartmentItemVo apartmentItemVo = apartmentInfoService.selectApartmentItemVoById(viewAppointment.getApartmentId());

            detailVo = new AppointmentDetailVo();
            BeanUtils.copyProperties(viewAppointment, detailVo);

            detailVo.setApartmentItemVo(apartmentItemVo);

            log.info("缓存未命中,查询数据库:{}", id);
            redis.opsForValue().set(key, detailVo,30, TimeUnit.MINUTES);
        }




        return detailVo;

    }
}




