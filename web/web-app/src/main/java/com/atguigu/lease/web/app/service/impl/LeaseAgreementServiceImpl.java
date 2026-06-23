package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.app.mapper.*;
import com.atguigu.lease.web.app.service.LeaseAgreementService;
import com.atguigu.lease.web.app.vo.agreement.AgreementDetailVo;
import com.atguigu.lease.web.app.vo.agreement.AgreementItemVo;
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
 * @description 针对表【lease_agreement(租约信息表)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
@Slf4j
public class LeaseAgreementServiceImpl extends ServiceImpl<LeaseAgreementMapper, LeaseAgreement>
        implements LeaseAgreementService {

    @Autowired
    private LeaseAgreementMapper leaseAgreementMapper;
    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;
    @Autowired
    private RoomInfoMapper roomInfoMapper;
    @Autowired
    private GraphInfoMapper graphInfoMapper;
    @Autowired
    private LeaseTermMapper leaseTermMapper;
    @Autowired
    private PaymentTypeMapper paymentTypeMapper;
    @Autowired
    private RedisTemplate<String, Object> redis;

    @Override
    public List<AgreementItemVo> listItemByPhone(String Phone) {
        return leaseAgreementMapper.listItemByPhone( Phone);

    }

    @Override
    public AgreementDetailVo getDetailById(Long id) {
        String key = RedisConstant.APP_AGREEMENT_PREFIX + id;
        AgreementDetailVo agreementDetailVo = (AgreementDetailVo) redis.opsForValue().get(key);
        if (agreementDetailVo == null) {
            LeaseAgreement leaseAgreement = leaseAgreementMapper.selectById(id);
            if (leaseAgreement == null) {
                return null;
            }
            //2.查询公寓信息
            ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(leaseAgreement.getApartmentId());

            //3.查询房间信息
            RoomInfo roomInfo = roomInfoMapper.selectById(leaseAgreement.getRoomId());

            //4.查询图片信息
            List<GraphVo> roomGraphVoList = graphInfoMapper.selectListByItemTypeAndId(ItemType.ROOM, leaseAgreement.getRoomId());
            List<GraphVo> apartmentGraphVoList = graphInfoMapper.selectListByItemTypeAndId(ItemType.APARTMENT, leaseAgreement.getApartmentId());

            //5.查询支付方式
            PaymentType paymentType = paymentTypeMapper.selectById(leaseAgreement.getPaymentTypeId());

            //6.查询租期
            LeaseTerm leaseTerm = leaseTermMapper.selectById(leaseAgreement.getLeaseTermId());
//            agreementDetailVo = new AgreementDetailVo();
            agreementDetailVo = new AgreementDetailVo();
            BeanUtils.copyProperties(leaseAgreement, agreementDetailVo);
            agreementDetailVo.setApartmentName(apartmentInfo.getName());
            agreementDetailVo.setRoomNumber(roomInfo.getRoomNumber());
            agreementDetailVo.setApartmentGraphVoList(apartmentGraphVoList);
            agreementDetailVo.setRoomGraphVoList(roomGraphVoList);
            agreementDetailVo.setPaymentTypeName(paymentType.getName());
            agreementDetailVo.setLeaseTermMonthCount(leaseTerm.getMonthCount());
            agreementDetailVo.setLeaseTermUnit(leaseTerm.getUnit());

            log.info("agreementDetailVo:{}");
            redis.opsForValue().set(key, agreementDetailVo,30, TimeUnit.MINUTES);
        }



        return agreementDetailVo;
    }
}




