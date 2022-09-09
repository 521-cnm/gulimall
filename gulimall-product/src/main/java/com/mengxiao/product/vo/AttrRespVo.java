package com.mengxiao.product.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class AttrRespVo extends AttrVo {
    private String catelogName;
    private String groupName;
    private Long[] catelogPath;
}
