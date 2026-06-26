package com.atguigu.lease.web.app.service;


import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface PayService {
    Map<String, Object> payWaterMark(Long roomId, Long mount);
}
