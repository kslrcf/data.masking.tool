package com.yinyuecheng.dataMasking.enums;

/**
 * @Author hang.zhang
 * @Date 2021/12/28 15:00
 * @Version 1.0
 */
public enum MaskingRuleEnum {
    MOBILE(1,"手机号，展示前三后四，中间脱敏"),
    ID_CARD(2,"身份证号码/护照/驾驶证号/社保账号  展示前六后四，中间脱敏"),
    BANK_CARD(3,"银行卡，社保卡 多余十位前六后四  少于十位展示后四"),
    EMAIL(4,"外部客户邮箱  @前三位脱敏。@后.cn和.com等前脱敏"),
    ADRESS(5,"地址长度不定，省市区显示不加密，涉及到具体地址几号几弄，几楼几号加密显示。（汉字数字）三号"),

    ;


    MaskingRuleEnum(Integer code,String desc){
        this.code=code;
        this.desc=desc;
    }

    private Integer code;
    private String desc;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
