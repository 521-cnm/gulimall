package com.common.constant;

public class WareConstant {
    public enum PurchaseStatusEnum {
        CREATED(0, "新建"), ASSIGNED(1, "已分配"),
        RECEIVE(2, "已领取"), FINISH(3, "已完成"),
        HASERRROR(4, "有异常");
        private int code;
        private String msg;

        PurchaseStatusEnum(int code, String smg) {
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

    public enum PurchaseDetailStatusEnum {
        CREATED(0, "新建"), ASSIGNED(1, "已分配"),
        BUYING(2, "正在采取"), FINISH(3, "已完成"),
        HASERRROR(4, "采购失败");
        private int code;
        private String msg;

        PurchaseDetailStatusEnum(int code, String smg) {
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
