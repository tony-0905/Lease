package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.eception.LeaseException;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.admin.mapper.*;
import com.atguigu.lease.web.admin.service.*;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentDetailVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentQueryVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentSubmitVo;
import com.atguigu.lease.web.admin.vo.fee.FeeValueVo;
import com.atguigu.lease.web.admin.vo.graph.GraphVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author liubo
 * @description 针对表【apartment_info(公寓信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Slf4j
@Service
public class ApartmentInfoServiceImpl extends ServiceImpl<ApartmentInfoMapper, ApartmentInfo>
        implements ApartmentInfoService {

    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;
    @Autowired
    private GraphInfoService graphInfoService;
    @Autowired
    private ApartmentFacilityService apartmentFacilityService;
    @Autowired
    private ApartmentLabelService apartmentLabelService;
    @Autowired
    private ApartmentFeeValueService apartmentfeevalueservice;
    @Autowired
    private GraphInfoMapper graphInfoMapper;
    @Autowired
    private LabelInfoMapper labelInfoMapper;
    @Autowired
    private FacilityInfoMapper facilityInfoMapper;
    @Autowired
    private FeeValueMapper feeValueMapper;
    @Autowired
    private RoomInfoMapper roomInfoMapper;
    @Autowired
    private RedisTemplate<String , Object> redis;

    @Override
    public void saveOrUpdateApartment(ApartmentSubmitVo apartmentSubmitVo) {

        super.saveOrUpdate(apartmentSubmitVo);

        boolean isUpdate  = apartmentSubmitVo.getId() != null;
        if(isUpdate){//是更新
            //删除图片列表
            LambdaQueryWrapper<GraphInfo> grapqueryWrapper = new LambdaQueryWrapper<>();
            grapqueryWrapper.eq(GraphInfo::getItemType, ItemType.APARTMENT);
            grapqueryWrapper.eq(GraphInfo::getItemId,apartmentSubmitVo.getId());
            graphInfoService.remove(grapqueryWrapper);

            //删除配套列表
            LambdaQueryWrapper<ApartmentFacility> apartmentFacilityQueryWrapper = new LambdaQueryWrapper<>();
            apartmentFacilityQueryWrapper.eq(ApartmentFacility::getApartmentId,apartmentSubmitVo.getId());
            apartmentFacilityService.remove(apartmentFacilityQueryWrapper);
            //删除标签列表
            LambdaQueryWrapper<ApartmentLabel> apartmentLabelQueryWrapper = new LambdaQueryWrapper<>();
            apartmentLabelQueryWrapper.eq(ApartmentLabel::getApartmentId,apartmentSubmitVo.getId());
            apartmentLabelService.remove(apartmentLabelQueryWrapper);
            //删除杂费列表
            LambdaQueryWrapper<ApartmentFeeValue> feeKeyQueryWrapper = new LambdaQueryWrapper<>();
            feeKeyQueryWrapper.eq(ApartmentFeeValue::getApartmentId,apartmentSubmitVo.getId());
            apartmentfeevalueservice.remove(feeKeyQueryWrapper);

        }
        //插入图片列插入
        List<GraphVo> graphVoList = apartmentSubmitVo.getGraphVoList();
        if(!CollectionUtils.isEmpty(graphVoList)){
            ArrayList<GraphInfo> graphInfoList = new ArrayList<>();
            for(GraphVo graphVo : graphVoList){
                GraphInfo graphInfo = new GraphInfo();
                graphInfo.setItemId(apartmentSubmitVo.getId());
                graphInfo.setItemType(ItemType.APARTMENT);
                graphInfo.setUrl(graphVo.getUrl());
                graphInfo.setName(graphVo.getName());
                graphInfoList.add(graphInfo);
            }
            graphInfoService.saveBatch(graphInfoList);
        }


        //插入配套列插入
        List<Long> idlist = apartmentSubmitVo.getFacilityInfoIds();
        if(!CollectionUtils.isEmpty(idlist)){
            ArrayList<ApartmentFacility> Facilitylist = new ArrayList<>();
            for(Long facilityid : idlist){

                ApartmentFacility apartmentFacility = new ApartmentFacility();
                apartmentFacility.setApartmentId(apartmentSubmitVo.getId());
                apartmentFacility.setFacilityId(facilityid);
                Facilitylist.add(apartmentFacility);
            }
            apartmentFacilityService.saveBatch(Facilitylist);
        }




        //3.插入标签列表
        List<Long> labelIds = apartmentSubmitVo.getLabelIds();
        if (!CollectionUtils.isEmpty(labelIds)) {
            List<ApartmentLabel> apartmentLabelList = new ArrayList<>();
            for (Long labelId : labelIds) {
                ApartmentLabel apartmentLabel = new ApartmentLabel();
                apartmentLabel.setApartmentId(apartmentSubmitVo.getId());
                apartmentLabel.setLabelId(labelId);
                apartmentLabelList.add(apartmentLabel);
            }
            apartmentLabelService.saveBatch(apartmentLabelList);
        }


        //4.插入杂费列表
        List<Long> feeValueIds = apartmentSubmitVo.getFeeValueIds();
        if (!CollectionUtils.isEmpty(feeValueIds)) {
            ArrayList<ApartmentFeeValue> apartmentFeeValueList = new ArrayList<>();
            for (Long feeValueId : feeValueIds) {
                ApartmentFeeValue apartmentFeeValue = new ApartmentFeeValue();
                apartmentFeeValue.setApartmentId(apartmentSubmitVo.getId());
                apartmentFeeValue.setFeeValueId(feeValueId);
                apartmentFeeValueList.add(apartmentFeeValue);
            }
            apartmentfeevalueservice.saveBatch(apartmentFeeValueList);
        }


    }

    @Override
    public IPage<ApartmentItemVo> pageItem(Page<ApartmentItemVo> page, ApartmentQueryVo queryVo) {
        return apartmentInfoMapper.pageItem(page, queryVo);
    }


    @Override
    public ApartmentDetailVo getDetailById(Long id) {
        String key = RedisConstant.ADMIN_APARTMENT_PREFIX + id;
        //查询公寓信息
        ApartmentDetailVo outapartmentDetailVo = (ApartmentDetailVo)redis.opsForValue().get(key);
        if(outapartmentDetailVo == null){
            ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(id);
            if(apartmentInfo==null)return null;
            ApartmentDetailVo apartmentDetailVo = new ApartmentDetailVo();
            //查询图片列表
            //方法1
//        LambdaQueryWrapper<GraphInfo> graphqueryWrapper = new LambdaQueryWrapper<>();
//        graphqueryWrapper.eq(GraphInfo::getItemType, ItemType.APARTMENT);
//        graphqueryWrapper.eq(GraphInfo::getItemId,id);
//        List<GraphInfo> Graphinfolist = graphInfoService.list(graphqueryWrapper);
            List<GraphVo> Graphinfolist = graphInfoMapper.selectListByItemtypeAndId(ItemType.APARTMENT,id);

            //查询标签列表
            List<LabelInfo> labelInfolist = labelInfoMapper.selectListByIdApartment(id);

            //查询配套列表
            List<FacilityInfo> facilityInfolist =  facilityInfoMapper.selectListByapartment(id);


            //查询杂费信息列表
            List<FeeValueVo> feeValueVoList = feeValueMapper.selectListByIdApartment(id);

            //组装结果返回

            BeanUtils.copyProperties(apartmentInfo, apartmentDetailVo);
            apartmentDetailVo.setGraphVoList(Graphinfolist);
            apartmentDetailVo.setFacilityInfoList(facilityInfolist);
            apartmentDetailVo.setLabelInfoList(labelInfolist);
            apartmentDetailVo.setFeeValueVoList(feeValueVoList);

            log.info("保存redis缓存:{}", apartmentDetailVo);
            try {
                redis.opsForValue().set(key, apartmentDetailVo,15, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.warn("Redis缓存写入失败:{}", e.getMessage());
            }
            outapartmentDetailVo= apartmentDetailVo;
        }


        //

        return outapartmentDetailVo;
    }

    @Override
    public void removeApartmentById(Long id) {
        //查询公寓中的房间个数
        LambdaQueryWrapper<RoomInfo> roomInfoQueryWrapper = new LambdaQueryWrapper<>();
        roomInfoQueryWrapper.eq(RoomInfo::getApartmentId,id);
        //公寓中的房间个数
        Long cnt = roomInfoMapper.selectCount(roomInfoQueryWrapper);
        if(cnt>0){
            //有房间，终止删除，并且相应提示信息
            throw new LeaseException(ResultCodeEnum.ADMIN_Apartment_DELETE_ERROR);
        }
        //删除apartmentinfo
        super.removeById(id);

        //删除图图片配套列表
        //删除图片列表
        LambdaQueryWrapper<GraphInfo> grapqueryWrapper = new LambdaQueryWrapper<>();
        grapqueryWrapper.eq(GraphInfo::getItemType, ItemType.APARTMENT);
        grapqueryWrapper.eq(GraphInfo::getItemId,id);
        graphInfoService.remove(grapqueryWrapper);

        //删除配套列表
        LambdaQueryWrapper<ApartmentFacility> apartmentFacilityQueryWrapper = new LambdaQueryWrapper<>();
        apartmentFacilityQueryWrapper.eq(ApartmentFacility::getApartmentId,id);
        apartmentFacilityService.remove(apartmentFacilityQueryWrapper);
        //删除标签列表
        LambdaQueryWrapper<ApartmentLabel> apartmentLabelQueryWrapper = new LambdaQueryWrapper<>();
        apartmentLabelQueryWrapper.eq(ApartmentLabel::getApartmentId,id);
        apartmentLabelService.remove(apartmentLabelQueryWrapper);
        //删除杂费列表
        LambdaQueryWrapper<ApartmentFeeValue> feeKeyQueryWrapper = new LambdaQueryWrapper<>();
        feeKeyQueryWrapper.eq(ApartmentFeeValue::getApartmentId,id);
        apartmentfeevalueservice.remove(feeKeyQueryWrapper);

        String adminKey = RedisConstant.ADMIN_APARTMENT_PREFIX + id;
        String appKey = RedisConstant.APP_APARTMENT_PREFIX + id;
        log.info("公寓信息被删除，删除缓存");
        redis.delete(adminKey);
        redis.delete(appKey);
    }
}




