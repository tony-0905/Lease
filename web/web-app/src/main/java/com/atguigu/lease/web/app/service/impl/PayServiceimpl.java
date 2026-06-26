package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.common.utils.QrCodeUtil;
import com.atguigu.lease.model.entity.PayWaterInfo;
import com.atguigu.lease.model.entity.RoomInfo;
import com.atguigu.lease.web.app.mapper.PaymentTypeMapper;
import com.atguigu.lease.web.app.mapper.RoomInfoMapper;
import com.atguigu.lease.web.app.service.PayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class PayServiceimpl implements PayService {

    @Autowired
    private RoomInfoMapper roomInfoMapper;
    @Autowired
    private PaymentTypeMapper paymentTypeMapper;

    @Override
    public Map<String, Object> payWaterMark(Long roomId, Long mount) {
        RoomInfo roomInfo = roomInfoMapper.selectById(roomId);
        if (roomInfo == null) {
            throw new IllegalArgumentException("房间不存在，roomId: " + roomId);
        }

        Long userId = LoginUserHolder.getLoginUser().getUserId();
        String username = LoginUserHolder.getLoginUser().getUsername();

        PayWaterInfo payWaterInfo = new PayWaterInfo();
        payWaterInfo.setUserId(userId);
        payWaterInfo.setUsername(username);
        payWaterInfo.setApartmentId(roomInfo.getApartmentId());
        payWaterInfo.setRoomId(roomId);
        payWaterInfo.setAmount(mount);
        payWaterInfo.setStatus(0);
        payWaterInfo.setCreateTime(LocalDateTime.now());
        payWaterInfo.setUpdateTime(LocalDateTime.now());
        paymentTypeMapper.savePatWaterInfo(payWaterInfo);

        String alipayUrl = "https://openapi.alipay.com/gateway.do?order_id=" + payWaterInfo.getId();
        String qrCodeBase64 = QrCodeUtil.generateBase64(alipayUrl);

        Map<String, Object> result = new HashMap<>();
        result.put("qrCodeBase64", qrCodeBase64);
        result.put("orderId", payWaterInfo.getId());

        return result;
    }
}
