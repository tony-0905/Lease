package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.app.mapper.*;
import com.atguigu.lease.web.app.service.ApartmentInfoService;
import com.atguigu.lease.web.app.service.BrowsingHistoryService;
import com.atguigu.lease.web.app.service.RoomInfoService;
import com.atguigu.lease.web.app.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.app.vo.attr.AttrValueVo;
import com.atguigu.lease.web.app.vo.fee.FeeValueVo;
import com.atguigu.lease.web.app.vo.graph.GraphVo;
import com.atguigu.lease.web.app.vo.room.RoomDetailVo;
import com.atguigu.lease.web.app.vo.room.RoomItemVo;
import com.atguigu.lease.web.app.vo.room.RoomQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author liubo
 * @description 针对表【room_info(房间信息表)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
@Slf4j
public class RoomInfoServiceImpl extends ServiceImpl<RoomInfoMapper, RoomInfo>
        implements RoomInfoService {

    @Autowired
    private RoomInfoMapper roomInfoMapper;
    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;
    @Autowired
    private GraphInfoMapper graphInfoMapper;
    @Autowired
    private AttrValueMapper attrValueMapper;
    @Autowired
    private FacilityInfoMapper facilityInfoMapper;
    @Autowired
    private LabelInfoMapper labelInfoMapper;
    @Autowired
    private PaymentTypeMapper paymentTypeMapper;
    @Autowired
    private  FeeValueMapper feeValueMapper;
    @Autowired
    private LeaseTermMapper leaseTermMapper;
    @Autowired
    private ApartmentInfoService apartmentInfoService;
    @Autowired
    private BrowsingHistoryService browsingHistoryService;

    @Autowired
    private RedisTemplate<String, Object> redis;

    @SuppressWarnings("rawtypes")
    @Override
    public IPage<RoomItemVo> pageItem(Page<RoomQueryVo> page, RoomQueryVo queryVo) {
        return roomInfoMapper.pageItem(page, queryVo);
    }

    @Override
    public RoomDetailVo getDtailById(Long id) {
        log.info("redis缓存查询:");
        String key = RedisConstant.APP_ROOM_PREFIX + id;
        RoomDetailVo roomDetailVo = (RoomDetailVo)redis.opsForValue().get(key);
        if(roomDetailVo == null){
            log.info("redis缓存未查询到:{}", id);
            log.info("getDtailById:{}", id);

            //1.查询房间信息
            RoomInfo roomInfo = roomInfoMapper.selectById( id);
            if(roomInfo == null)return null;
            //2.查询公寓图片信息
            List<GraphVo> graphlist = graphInfoMapper.selectListByItemTypeAndId(ItemType.ROOM, id);

            //3.查询属性信息
            List<AttrValueVo> attrValueList = attrValueMapper.selectListByRoomId(ItemType.ROOM, id);

            //4.查询配套信息
            List<FacilityInfo> facilityInfoList = facilityInfoMapper.selectListByRoomId(ItemType.ROOM, id);

            //5.查询标签信息
            List<LabelInfo> labelInfoList = labelInfoMapper.selectListByRoomId(id);

            //6.查询支付方式信息
            List<PaymentType> paymentTypeList = paymentTypeMapper.selectListByRoomId(id);

            //7.查询杂费信息
            List<FeeValueVo> feeValueVoList = feeValueMapper.selectListByRoomId(id);

            //8.查询租期信息
            List<LeaseTerm> leaseTermList = leaseTermMapper.selectListByRoomId(id);

            //9.查询公寓信息
            ApartmentItemVo apartmentItemVo = apartmentInfoService.selectApartmentItemVoById(roomInfo.getApartmentId());
             roomDetailVo = new RoomDetailVo();
            BeanUtils.copyProperties(roomInfo, roomDetailVo);
            roomDetailVo.setApartmentItemVo(apartmentItemVo);
            roomDetailVo.setGraphVoList(graphlist);
            roomDetailVo.setAttrValueVoList(attrValueList);
            roomDetailVo.setFacilityInfoList(facilityInfoList);
            roomDetailVo.setLabelInfoList(labelInfoList);
            roomDetailVo.setPaymentTypeList(paymentTypeList);
            roomDetailVo.setFeeValueVoList(feeValueVoList);
            roomDetailVo.setLeaseTermList(leaseTermList);
            log.info("保存redis缓存:{}", roomDetailVo);
            redis.opsForValue().set(key, roomDetailVo);
        }




        log.info("保存用户浏览历史");
        browsingHistoryService.saveHistory(LoginUserHolder.getLoginUser().getUserId(),id);



        return roomDetailVo;
    }

    @Override
    public IPage<RoomItemVo> pageItemByApartmentId(Page<RoomQueryVo> page, Long id) {
        return roomInfoMapper.pageItemByApartmentId(page, id);
    }
}




