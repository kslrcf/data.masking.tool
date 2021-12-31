package com.yinyuecheng.dataMasking.handle;

import com.yinyuecheng.dataMasking.AppEncrypt.AES128CommonUtils;
import com.yinyuecheng.dataMasking.annotation.DataMaskingField;
import com.yinyuecheng.dataMasking.enums.DataMaskingType;
import com.yinyuecheng.dataMasking.enums.MaskingRuleEnum;
import com.yinyuecheng.dataMasking.redis.RedisTools;
import org.apache.commons.collections4.CollectionUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @Author hang.zhang
 * @Date 2021/12/28 15:14
 * @Version 1.0
 */
@Aspect
@Component
public class DataMaskingHandle {
    private static final String SYMBOL = "*";

    private RedisTools redisTools;

    private boolean needSearch =false;

    private String prefix = "kslrcf";

    private String separate = "$kslrcf$";

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    private String path = "com.yinyuecheng";

    public String getSeparate() {
        return separate;
    }

    public void setSeparate(String separate) {
        this.separate = separate;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public boolean isNeedSearch() {
        return needSearch;
    }

    public void setNeedSearch(boolean needSearch) {
        this.needSearch = needSearch;
    }

    public RedisTools getRedisTools() {
        return redisTools;
    }

    public void setRedisTools(RedisTools redisTools) {
        this.redisTools = redisTools;
    }

    @Pointcut("@annotation(com.zhaogang.dataMasking.annotation.DataMasking)")
    public void DataMaskingAroundPointCut(){}

    @Around("DataMaskingAroundPointCut()")
    public Object validAround(ProceedingJoinPoint joinPoint) {
        //获取目标方法的注解及其属性
        Object obj =null;
        try {
             obj= joinPoint.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        try {
            choose(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }


    /**
     * 遍历复杂对象
     */
    private void  choose(Object  obj) throws Exception{
        if(null==obj||checkObjectIsSysType(obj)){
            return;
        }
        Class clazz = obj.getClass();
        Field[] declaredFields = clazz.getDeclaredFields();
        for(Field field:declaredFields){
            field.setAccessible(true);
            String type = field.getGenericType().toString();
            //复杂对象
            if(type.contains(path)){
                choose(field.get(obj));
            }
            if(type.contains("java.util.List")){
                List<Object> list = (List)field.get(obj);
                if(CollectionUtils.isNotEmpty(list)){
                    for(Object obj1 :list){
                        choose(obj1);
                    }
                }
            }
            DataMaskingField maskingField = field.getAnnotation(DataMaskingField.class);
            if(null!=maskingField){
                if("class java.lang.String".equals(type)){
                    String fieldData = (String) field.get(obj);
                    DataMaskingType maskingType = maskingField.maskingType();
                    if(maskingType==DataMaskingType.ENCRYPT){
                        //加密
                        String AESFieldData = AES128CommonUtils.encrypt(fieldData,AES128CommonUtils.IV_STRING);
                        field.set(obj,AESFieldData);
                    }else{
                        //脱敏
                        String replaceFieldData = replace(fieldData,maskingField.rule());
                        if(needSearch){
                            String mark = prefix+replaceFieldData.hashCode()+System.currentTimeMillis();
                            redisTools.setString(mark,fieldData);
                            field.set(obj,replaceFieldData+separate+mark);
                        }else{
                            field.set(obj,replaceFieldData);
                        }
                    }
                }
            }
        }
    }

    /**
     * 替换值
     * 时间紧迫  先case
     * @param str
     * @param rule
     * 身份证号码/护照/驾驶证号/社保账号	"1.中国大陆身份证18/15位，前六后四明文显示，其余脱敏*
     * 例如：310210********3412；310120******1232
     * 2.护照和其他：后四位明文，其余脱敏。"
     * 手机号/登录名（包括以手机号作为用户登录名的）	"1.中国大陆手机长度11位数字，前三后四明文显示，其余脱敏
     * 例如：135****6688
     * 国家代码无需：086
     * 2.港澳台和其他格式：数字长度不定，后四位明码，其余脱敏。
     * 3.小于5位全部脱敏显示。"
     * 银行卡/社保卡	"1.银行卡号长度不定，号位位数大于10位，号码前六后四位明文显示，其他位脱敏显示。
     * 2.银行名称可直接显示。
     * 3.持卡人姓名明文显示
     * 例如：中国农业银行662312**********2342张三
     * 4.资金平台、回单小助手可明文(需看具体页面）
     * 5.小于等于10位，后四位明码，其余脱敏。"
     * 外部客户邮箱	"1.邮箱长度不定，@前三位脱敏。@后.cn和.com等前脱敏
     * 例如：zuoming***@***.com ，**@***.com
     * 2.oa、hr系统内部人员公司邮箱（邮箱后缀@zhaogangcom，@china.zhaogang.cn）无需脱敏显示"
     * 详细地址	"1.公司地址可明文显示（业务系统地址不脱敏）
     * 2.HR、OA系统，个人地址需脱敏。地址长度不定，省市区显示不加密，涉及到具体地址几号几弄，几楼几号加密显示。（汉字数字）三号
     * 例如：上海市嘉定区新培路***号**楼**室
     * 上海市黄浦区中山东路**弄**号**楼**室"
     * 个人微信/qq	"1.个人微信号/qq需脱敏显示
     * 2.最后四位显示明文，其余脱敏
     * 3.合同导出后，可明文显示。预览需脱敏。"
     */
    private String replace(String str, MaskingRuleEnum rule){
        StringBuilder sb;
        String replaceStr = null;
        switch (rule){
            case EMAIL:

                break;
            case MOBILE:
                sb = new StringBuilder(str);
                sb.replace(3,str.length()-4,getSymbols(str.length()-4-3));
                replaceStr= sb.toString();
                break;
            case ID_CARD:
                sb = new StringBuilder(str);
                sb.replace(6,str.length()-4,getSymbols(str.length()-4-6));
                replaceStr= sb.toString();
                break;
            case BANK_CARD:
                if(str.length()>10){
                    sb = new StringBuilder(str);
                    sb.replace(6,str.length()-4,getSymbols(str.length()-4-6));
                    replaceStr= sb.toString();
                    break;
                }else{
                    sb = new StringBuilder(str);
                    sb.replace(0,str.length()-4,getSymbols(str.length()-4));
                    replaceStr=  sb.toString();
                    break;
                }
            case ADRESS:
                break;

        }
        return replaceStr;
    }


    public static boolean checkObjectIsSysType(Object object){
        String objType = object.getClass().toString();
        if ("byte".equals(objType) || "short".equals(objType) || "int".equals(objType)|| "long".equals(objType)|| "double".equals(objType) || "float".equals(objType) || "boolean".equals(objType)){
            return true;
        }else if ("class java.lang.Byte".equals(objType) || "class java.lang.Short".equals(objType) ||
                "class java.lang.Integer".equals(objType) || "class java.lang.Long".equals(objType) ||
                "class java.lang.Double".equals(objType) || "class java.lang.Float".equals(objType) ||
                "class java.lang.Boolean".equals(objType) || "class java.lang.String".equals(objType)){
            return true;
        }else {
            return  false;
        }

    }

    private String getSymbols(int size){
        StringBuilder sb = new StringBuilder();
        for(int i =0;i<size;i++){
            sb.append(SYMBOL);
        }
        return sb.toString();
    }

}
