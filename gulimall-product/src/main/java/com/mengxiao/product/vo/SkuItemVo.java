package com.mengxiao.product.vo;

import com.mengxiao.product.entity.SkuImagesEntity;
import com.mengxiao.product.entity.SkuInfoEntity;
import com.mengxiao.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

@Data
public class SkuItemVo {
    //sku基本信息
    SkuInfoEntity info;
    boolean hasStock = true;
    //sku图片信息
    List<SkuImagesEntity> images;
    //sku的销售属性组合
    List<SkuItemSaleAttrVo> saleAttr;
    //spu的介绍
    SpuInfoDescEntity desp;
    //spu的规格参数信息
    List<SpuItemAttrGroupVo> groupAttrs;
    //当前商品的秒杀优惠信息
    SeckillInfoVo seckillInfo;

}
