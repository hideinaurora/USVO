package org.example.utils.generator;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.converts.MySqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.querys.MySqlQuery;
import com.baomidou.mybatisplus.generator.fill.Column;
import com.baomidou.mybatisplus.generator.keywords.MySqlKeyWordsHandler;

import java.util.Collections;

public class CodeGenerator {

    public static void genTableTemplate(GenEntity entity) {
        // 数据库连接
        String sqlUrl = "jdbc:" + entity.getDbType() + "://" + entity.getUrl() + ":" + entity.getPort() +
                "/" + entity.getDbName() + entity.getDbUrlParams();
        // 获取用户基本路径
        String basePath = System.getProperty("user.dir");
        // 接口适用用户（web/app）
        for (String t : entity.getTables()) {
            String[] strings = t.split("_");
            String packSuffix = "." + strings[0] + "." + strings[1];
            FastAutoGenerator.create(
                            //数据源配置，url需要修改
                            new DataSourceConfig.Builder(
                                    sqlUrl, entity.getUserName(), entity.getUserPwd())
                                    .dbQuery(new MySqlQuery())
                                    .schema(entity.getDbName())
                                    .typeConvert(new MySqlTypeConvert())
                                    .keyWordsHandler(new MySqlKeyWordsHandler()))
                    //全局配置
                    .globalConfig(builder -> {
                        builder.author("ckd") // 设置作者
                                .disableOpenDir()//禁止打开输出目录
                                .enableSwagger() // 开启 swagger 模式
                                .fileOverride() // 覆盖已生成文件
                                .outputDir(basePath + entity.getDirPath() + "/src/main/java"); // 指定输出目录
                    })
                    //包配置
                    .packageConfig(builder -> {
                        builder.parent(entity.getModulePath()) // 设置父包名，根据实制项目路径修改
                                .moduleName(entity.getModuleName())      // 父包名路径下再新建的文件夹
                                .entity("entity" + packSuffix)         // 后面这些是sys文件夹里新建的各分类文件夹
                                .service("service" + packSuffix)
                                .serviceImpl("service" + packSuffix + ".impl")
                                .mapper("mapper" + packSuffix)
                                .xml("mapper.xml")
                                .pathInfo(Collections.singletonMap(OutputFile.mapperXml,
                                        basePath + entity.getDirPath() + "/src/main/resources/mapper")); // 存放mapper.xml路径
                    }).templateConfig(builder -> {
                        builder.disable()
                                .entity("template/entity.java")
                                .service("template/service.java")
                                .serviceImpl("template/serviceImpl.java")
                                .mapper("template/mapper.java")
                                .mapperXml("template/mapper.xml")
                                .build();
                    })
                    //策略配置
                    .strategyConfig(builder -> {
                        builder.addInclude(t)// 设置需要生成的表名
                                .addTablePrefix(strings[0])
                                //.addTablePrefix("tb_", "c_") // 设置过滤表前缀
                                .entityBuilder() //实体类配置
                                .addIgnoreColumns(entity.getIgnoreCol())
                                .idType(IdType.AUTO)
                                .logicDeleteColumnName(entity.getLogicDeleteField())
                                .versionColumnName("version")
                                .addTableFills(new Column("gmt_create", FieldFill.INSERT), new Column("gmt_modify", FieldFill.INSERT_UPDATE)) // 自动填充字段配置
                                .formatFileName("%sEntity")
                                .enableLombok() //使用lombok
                                .enableTableFieldAnnotation()//实体类字段注解
                                .serviceBuilder()
                                .formatServiceFileName("%sService")
                                .formatServiceImplFileName("%sServiceImpl")
                                .mapperBuilder()
                                .convertMapperFileName((entityName) -> String.format("%sMapper", entityName))
                                .convertXmlFileName((entityName) -> String.format("%sMapper", entityName))
                                .enableMapperAnnotation()//开启mapper注解
                                .enableBaseResultMap()//启用 BaseResultMap 生成
                                .enableBaseColumnList();//启用 BaseColumnList
                    }).execute();
        }
    }

    public static void main(String[] args) {
        GenEntity entity = new GenEntity();
        // 数据库基本信息，账号，地址等
        entity.setPort("33068");
        entity.setUrl("192.168.10.141");
        entity.setDbName("nblg_apply");
        entity.setUserName("root");
        entity.setUserPwd("ibdw@1608");
        // 生成路径信息
        // 项目基本路径
        entity.setModulePath("org.example");
        // 生成文件项目内路径
        entity.setDirPath("/");
        // 生成文件前缀路径，默认data
        entity.setModuleName("");
        // 生成文件名配置信息
        entity.setTables(new String[]{"basic_user"});
        entity.setLogicDeleteField("is_deleted");
        genTableTemplate(entity);
    }
}
