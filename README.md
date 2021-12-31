# data.masking.tool
敏感数据脱敏工具包，包含手机号，身份证，银行卡，邮箱等，支持明文缓存（适配点击查看明文功能 需要前端配合修改），单纯脱敏只需要注解即可

食用方法：
1：emmmm 先打个包然后项目引用。。。
2：如果有查看明文需求则需要实现RedisTools接口并注入到DataMaskingHandle，若没有则下一步
3：配置DataMaskingHandle属性  一共有五个属性
（1）：needSearch 默认为false，为true时会将操作字段编辑成 脱敏字符串+分隔符+Key的形式，否则只返回脱敏字符串且不会缓存
（2）：redisTools 无默认值  不配置且needSearch为true时会异常（异常被catch会直接返回明文），值为上面描述的redisTools的实现类
（3）：separate分隔符  默认值为$kslrcf$ 主要是为了分隔脱敏串与key
（4）：prefix 前缀 默认值为kslrcf 主要用于拼接key  区分业务线
（5）：path 包路径 默认值为com.yinyuecheng 用于复杂对象嵌套时遍历，避免全量遍历（一定要改！我根本没有写全量遍历！！）暂时只支持一个包，
    后续会支持逗号分隔。。。
    ps：单纯脱敏的话所有配置都无需改动 
4：依赖注入啊 切面配置啥的

配置完成后：
1：对需要数据处理的方法添加注解@DataMasking
2：对方法返回对象中需要处理的具体字段添加注解@DataMaskingField
@DataMasking 无属性
@DataMaskingField 有两个属性
（1）：rule  对应脱敏规则 有五个值  标记后字段会根据对应规则脱敏
MOBILE(1,"手机号，展示前三后四，中间脱敏"),
ID_CARD(2,"身份证号码/护照/驾驶证号/社保账号  展示前六后四，中间脱敏"),
BANK_CARD(3,"银行卡，社保卡 多余十位前六后四  少于十位展示后四"),
EMAIL(4,"外部客户邮箱  @前三位脱敏。@后.cn和.com等前脱敏"),
ADRESS(5,"地址长度不定，省市区显示不加密，涉及到具体地址几号几弄，几楼几号加密显示。（汉字数字）三号"),
暂时不支持自定义规则，后续会更新。。。

（2）：maskingType 对应脱敏方案  有两个值
ENCRYPET, 加密
REPLACE, 脱敏
一般应该只需要脱敏  加密的话又要支持自定义。。。

啊  我真懒


