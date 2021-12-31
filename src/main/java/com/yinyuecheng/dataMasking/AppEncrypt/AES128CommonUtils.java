package com.yinyuecheng.dataMasking.AppEncrypt;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by junming.qi on 2017/2/16.
 */
public class AES128CommonUtils {
    public static final String IV_STRING = "2d8943939bd8840a";

    public static String encrypt(String content, String key)
            throws Exception {
        // 判断Key是否正确
        if (key == null) {
            throw new Exception("aes128加密Key为空null");
        }
        // 判断Key是否为16位
        if (key.length() != 16) {
            throw new Exception("aes128加密Key长度不是16位");
        }
        byte[] byteContent = content.getBytes("UTF-8");
// 注意，为了能与 iOS 统一
// 这里的 key 不可以使用 KeyGenerator、SecureRandom、SecretKey 生成
        byte[] enCodeFormat = key.getBytes();
        SecretKeySpec secretKeySpec = new SecretKeySpec(enCodeFormat, "AES");
        byte[] initParam = IV_STRING.getBytes();
        IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);
// 指定加密的算法、工作模式和填充方式
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] encryptedBytes = cipher.doFinal(byteContent);
// 同样对加密后数据进行 base64 编码
        return new Base64().encodeToString(encryptedBytes);
    }

    public static String decrypt(String content, String key)
            throws Exception {
        // 判断Key是否正确
        if (key == null) {
            throw new Exception("aes128加密Key为空null");
        }
        // 判断Key是否为16位
        if (key.length() != 16) {
            throw new Exception("aes128加密Key长度不是16位");
        }
// base64 解码
        byte[] encryptedBytes = new Base64().decode(content);
        byte[] enCodeFormat = key.getBytes();
        SecretKeySpec secretKey = new SecretKeySpec(enCodeFormat, "AES");
        byte[] initParam = IV_STRING.getBytes();
        IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
        byte[] result = cipher.doFinal(encryptedBytes);
        return new String(result, "UTF-8");
    }

    public static void main(String[] args) throws Exception {
        /*
         * 此处使用AES-128-ECB加密模式，key需要为16位。
         */
        // 需要加密的字串
        String cSrc = "13641489859";
        System.out.println(cSrc);
//        cSrc = Base64Utils.encode(cSrc);
//        System.out.println(cSrc);
        // 加密
        String enString = encrypt(cSrc, AES128CommonUtils.IV_STRING);
        System.out.println("加密后的字串是：" + enString);

        // 解密
        String DeString = decrypt(enString, AES128CommonUtils.IV_STRING);
        System.out.println("解密后的字串是：" + DeString);
    }

}
