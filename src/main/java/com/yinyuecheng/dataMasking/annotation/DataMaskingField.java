package com.yinyuecheng.dataMasking.annotation;

import com.yinyuecheng.dataMasking.enums.DataMaskingType;
import com.yinyuecheng.dataMasking.enums.MaskingRuleEnum;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author hang.zhang
 * @Date 2021/12/29 11:16
 * @Version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface DataMaskingField {
    MaskingRuleEnum rule() default MaskingRuleEnum.BANK_CARD   ;
    DataMaskingType maskingType() default DataMaskingType.REPLACE ;

}
