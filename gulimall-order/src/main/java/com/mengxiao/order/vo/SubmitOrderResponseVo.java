package com.mengxiao.order.vo;

import com.mengxiao.order.entity.OrderEntity;
import lombok.Data;

@Data
public class SubmitOrderResponseVo {
    private OrderEntity order;
    private Integer code;//错误状态码 0成功
}
