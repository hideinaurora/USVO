package org.example.utils.generator;

import lombok.Data;

@Data
public class GenEntity {
    /**
     * 数据库地址
     */
    private String url;
    /**
     * 端口号
     */
    private String port;
    /**
     * 数据库名称
     */
    private String dbName;
    /**
     * 数据库类型
     */
    private String dbType = "mysql";
    /**
     * 数据库用户
     */
    private String userName;
    /**
     * 数据库密码
     */
    private String userPwd;
    /**
     * 包路径
     */
    private String modulePath;
    /**
     * 文件路徑
     */
    private String dirPath;
    /**
     * 模块名称
     */
    private String moduleName = "data";
    /**
     * 忽视字段
     */
    private String ignoreCol = "";
    /**
     * 生成表名
     */
    private String[] tables;
    /**
     * 逻辑删除字段
     */
    private String logicDeleteField = "is_deleted";
    /**
     * 数据库参数
     */
    private String dbUrlParams = "?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai";
}
