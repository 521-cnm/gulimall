package com.common.constant;

import org.mybatis.spring.annotation.MapperScan;
import com.alibaba.fastjson.JSON;

public class ProductConstant {
    public enum AttrEnum {
        ATTR_TYPE_BASE(1, "基本属性"), ATTR_TYPE_SALE(0, "销售属性");
        private int code;
        private String msg;

        AttrEnum(int code, String smg) {
            this.code = code;
            this.msg = smg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }

    public enum StatusEnum {
        NEW_SPU(0, "新建"), SPU_UP(1, "商品上架"), SPU_DOWN(2, "商品下架");
        private int code;
        private String msg;

        StatusEnum(int code, String smg) {
            this.code = code;
            this.msg = smg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }
}
