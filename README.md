# Paging-Plugin
This is a paging assistant plug-in developed based on MyBatis and MyBatis-Plus plug-ins, designed to simplify the implementation of paging queries.

[TOC]

### 一、bin-page-plugin
> 1、这是一个基于 MyBatis 和 MyBatis-Plus 插件开发的分页辅助插件，旨在简化分页查询的实现过程。  
> 2、无侵入式改变 SQL 语句：插件可以在不修改原始 SQL 语句的情况下，实现分页效果和结果数量统计。  
> 3、拦截器形式：插件以拦截器的形式工作，能够拦截查询请求并对其进行分页处理，从而实现分页功能的无缝集成。  
> 4、该插件的引入可以让分页查询的实现变得更加简单和高效，同时避免了手动编写分页逻辑的繁琐和重复性工作。  



### 二、依赖引用

Maven依赖：
> 1、bin-page-starter 是分页插件的启动器，分页插件的核心功能。

```xml
<dependency>
    <groupId>org.example</groupId>
    <artifactId>bin-page-starter</artifactId>
    <version>1.0.0</version>
</dependency>

```


> 2、返回实体 Common 的基础模块依赖。

```xml
<dependency>
    <groupId>org.example</groupId>
    <artifactId>bin-page-commons</artifactId>
    <version>1.0.0</version>
</dependency>

```

### 三、配置的使用
1）该插件默认启用，无需额外配置。如果需要关闭插件，只需将配置项 binPlugin.enable 修改为 false 即可。
```yml
binPlugin:
#分页插件开关(默认开启)
  pagingPlugin:
    enable: false
```  

2）分页信息的配置是可选的，如果没有配置，则会使用默认的配置项名称。  
```yml
binPlugin:
  pagingPlugin:
    #设置分页名称(可不配置)
    name:
      # 页数名称设置
      # 页码
      pageNum: pageNum
      # 每页数量
      pageSize: pageSize
      # 总页数
      total: total
      # 总记录数
      count: count
```  

3）客户端在进行分页查询时，需要将参数与配置项对应起来。客户端默认只需在请求头中添加两个参数：  
> 1、页码：pageNum，需要传入一个数字作为页码。  
> 2、每页条数：pageSize，需要传入一个数字作为每页的数据条数。  
3、注意：目前该插件仅支持使用 GET 请求进行分页查询，后续可能会进行升级以支持更多的请求方式。  
```html
GET https://172.0.0.0:8888/xxxxx/xxx?pageSize=10&pageNum=2
```

4）返回信息
> 为了实现分页信息的返回，返回对象必须继承 PageResult 类。这个类包含了分页信息的各项属性。  
> 在查询时，插件会将查询结果封装到 PageResult 对象中，并自动计算出分页信息。  
> 通过继承 PageResult 类，查询结果就可以包含分页信息了。
```json
{
  "page": {
    "total": 2,
    "count": 11,
    "pageSize": 10,
    "pageNum": 2
  },
  "code": 20000,
  "msg": "成功",
  "data": [
    {
      "code": "lzb2",
      "name": "LZB"
    }
  ]
}
```

5）所有可配置的属性：  
```yml
binPlugin:
  pagingPlugin:
    #分页插件开关(默认开启)
    enable: false
    #设置分页名称(可不配置)
    name:
      # 页数名称设置
      # 页码
      pageNum: pageNum
      # 每页数量
      pageSize: pageSize
      # 总页数
      total: total
      # 总记录数
      count: count
```  
