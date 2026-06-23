package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.model.entity.ApartmentInfo;
import com.atguigu.lease.model.entity.FacilityInfo;
import com.atguigu.lease.model.entity.LabelInfo;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.app.mapper.*;
import com.atguigu.lease.web.app.service.ApartmentInfoService;
import com.atguigu.lease.web.app.vo.apartment.ApartmentDetailVo;
import com.atguigu.lease.web.app.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.app.vo.graph.GraphVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author liubo
 * @description 针对表【apartment_info(公寓信息表)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
@Slf4j
public class ApartmentInfoServiceImpl extends ServiceImpl<ApartmentInfoMapper, ApartmentInfo>
        implements ApartmentInfoService {
    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;
    @Autowired
    private GraphInfoMapper graphInfoMapper;
    @Autowired
    private RoomInfoMapper roomInfoMapper;
    @Autowired
    private LabelInfoMapper labelInfoMapper;
    @Autowired
    private FacilityInfoMapper facilityInfoMapper;

    @Autowired
    private RedisTemplate<String, Object> redis;

    @Override
    public ApartmentItemVo selectApartmentItemVoById(Long id) {

        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(id);

        List<LabelInfo> labelInfoList = labelInfoMapper.selectListByApartmentId(id);

        List<GraphVo> graphVoList = graphInfoMapper.selectListByItemTypeAndId(ItemType.APARTMENT, id);

        BigDecimal minRent = roomInfoMapper.selectMinRentByApartmentId(id);

        ApartmentItemVo apartmentItemVo = new ApartmentItemVo();
        BeanUtils.copyProperties(apartmentInfo, apartmentItemVo);

        apartmentItemVo.setGraphVoList(graphVoList);
        apartmentItemVo.setLabelInfoList(labelInfoList);
        apartmentItemVo.setMinRent(minRent);
        return apartmentItemVo;
    }

    @Override
    public ApartmentDetailVo getDtailById(Long id) {
        String key = RedisConstant.APP_APARTMENT_PREFIX + id;
        ApartmentDetailVo apartmentDetailVo = (ApartmentDetailVo) redis.opsForValue().get(key);
        if (apartmentDetailVo == null) {
            log.info("redis缓存未查询到:{}", id);

            ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(id);
            if (apartmentInfo == null) {
                return null;
            }

            List<GraphVo> graphVoList = graphInfoMapper.selectListByItemTypeAndId(ItemType.APARTMENT, id);
            List<LabelInfo> labelInfoList = labelInfoMapper.selectListByApartmentId(id);
            List<FacilityInfo> facilityInfoList = facilityInfoMapper.selectListByRoomId(ItemType.APARTMENT, id);
            BigDecimal minRent = roomInfoMapper.selectMinRentByApartmentId(id);

            apartmentDetailVo = new ApartmentDetailVo();
            BeanUtils.copyProperties(apartmentInfo, apartmentDetailVo);
            apartmentDetailVo.setFacilityInfoList(facilityInfoList);
            apartmentDetailVo.setGraphVoList(graphVoList);
            apartmentDetailVo.setLabelInfoList(labelInfoList);
            apartmentDetailVo.setMinRent(minRent);

            redis.opsForValue().set(key, apartmentDetailVo);
        }

        return apartmentDetailVo;

    }
}



