package com.atguigu.lease.web.admin.custom.converter;

import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.model.enums.LeaseStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToLeaseStatusTypeConverter implements Converter<String, LeaseStatus> {


    @Override
    public LeaseStatus convert(String code) {

        for (LeaseStatus value : LeaseStatus.values()) {
            if (value.getCode().equals(Integer.valueOf(code))) {
                return value;
            }
        }
        throw new IllegalArgumentException("code非法");
    }


}