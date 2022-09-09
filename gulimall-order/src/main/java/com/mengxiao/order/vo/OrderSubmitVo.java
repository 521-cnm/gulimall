package com.mengxiao.order.vo;

import lombok.Data;
import org.graalvm.compiler.replacements.IntrinsicGraphBuilder;

import java.math.BigDecimal;

/**
 * 封装订单提交的数据
 */
@Data
public class OrderSubmitVo {
    private Long addrId;//收货地址id
    private Integer payType;//支付方式
    private String orderToken;//防重令牌
    private BigDecimal payPrice;//应付价格
    private String note;//订单备注
}
