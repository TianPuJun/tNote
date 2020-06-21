# 环境搭建
# mysql  
数据库名： mynote
生产数据库：mynote_pro
账号： root
密码： root
端口： 3306
IP： localhost
初始化sql：见note.sql文件
# redis
ip： localhost
端口：6379
密码： redis ，redis没有配置密码此处可以去掉
# mongodb
ip： localhost
端口： 27017
库名： tNote
生产： tNote_pro
# 启动
环境连接好，启动MyNoteApplication即可
# swagger文档访问
http://localhost:2000/swagger-ui.html
# generator生成代码
1. 打开本目录下resources/generator/generatorConfig.xml文件
2. 修改绝对地址，targetProject为生成的代码位置，targetPackage为目标包名
`

        <javaModelGenerator targetPackage="com.tz.entity.bean"
                            targetProject="/cxt/codework/mywork/tNote/tNote-server/tNote-entity/src/main/java"/>

        <sqlMapGenerator targetPackage="mapper"
                         targetProject="/cxt/codework/mywork/tNote/tNote-server/tNote-dao/src/main/resources"/>

        <javaClientGenerator targetPackage="com.tz.dao.dao"
                             targetProject="/cxt/codework/mywork/tNote/tNote-server/tNote-dao/src/main/java"
                             type="XMLMAPPER"/>
`
3. 修改需要创建的表名
![image](https://github.com/TianPuJun/tNote/blob/more-module/tNote-server/tNote-generator/src/img/WX20190910-094829%402x.png)
<table tableName="note_log"><generatedKey column="id" sqlStatement="JDBC"/></table>
4. 运行maven插件

![image](https://github.com/TianPuJun/tNote/blob/more-module1/tNote-server/tNote-generator/src/img/WX20190910-095137%402x.png)

5. docker 部署
第一步：先package 打包生成jar文件
第二步：配置docker插件配置，运行生成镜像和容器
配置截图
![image](src/main/resources/img/docker_run_setter.png)
6. 运行命令指定配置文件
* --spring.profiles.active=dev
* 容器构建时指定环境变量,开发环境dev，**生产prod**
* -e SPRING_PROFILES_ACTIVE="prod"

7. run options

```text
-e SPRING_PROFILES_ACTIVE="prod" --link tz_mysql:tz_mysql --link tz_redis:tz_redis --link tz_mongo:tz_mongo
```


# 启动程序加入skywalking 监控
* 使用时使用Dockerfile2，为了在docker容器中无法在sky中注册，需要指定 [SW_AGENT_COLLECTOR_BACKEND_SERVICES] oap地址，默认127.0.0.1:11800