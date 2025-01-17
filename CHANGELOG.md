# Next

### Fetures

* 增强 `DefaultSqlExecutor`，新增 `setTransactionIsolation(int level)` 方法，可配置隔离级别
* 增强 `DateFormatFieldConvertor`，使支持 `Temporal` 及其子类的对象的格式化
* 增强 `DateFormatFieldConvertor`，新增：`setZoneId(ZoneId)` 方法，可配置时区
* 增强 `bean-searcher-boot-starter`，自动配置一些常用的字段转换器

# v3.0.1

### Fetures
* DateFormatFieldConvertor 新增 setFormat 方法

### Bug Fix
* 修复：v3.0.0 中，再没有指定 @SearchBean 注解的 joinCond 属性时，带条件的 SQL 生成中 where 后少一个 左括号的问题

# v3.0.0 重大更新

#### 新特性概览

* 支持 热加载
* 支持 无注解
* 支持 Select 指定字段
* 支持 条件与运算符的约束
* 支持 参数过滤器
* 支持 字段转换器
* 支持 Sql 拦截器
* 支持 多数据源
* 支持 JDK 9+

#### Bean Searcher

* 精简 Searcher 接口，移除一些无用的方法（最后一个形参为 `prefix` 的检索方法被移除）
* 架构优化：SearchBean 支持热加载，在配置了热加载的应用开发中，SearchBean 修改后，无需重启即可生效
* 移除 `SearchPlugin` 与 `SpringSearcher` 辅助类，因为 v3.0 的 Bean Searcher 的使用比借助辅助类更加容易
* 精简 `SearchResult` 类，移除没有必要的字段，只保留 `totalCount`、`dataList` 与 `summaries` 字段
* 新增 `Searcher` 的子接口：`MapSearcher` 与 `BeanSearcher` 与其相关实现，`MapSearcher` 中的检索方法返回的数据类型为 `Map`, `BeanSearcher` 中的检索方法返回的数据类型为泛型的 Search Bean
* 重构 `SearcherBuilder` 构建器, 使其更容易构建出一个 `MapSearcher` 或 `BeanSearcher` 实例
* 注解 `@SearchBean` 的 `groupBy` 属性，支持嵌入参数，嵌入参数未传入时，使用空字符串（以前使用 `"null"` 字符串）
* 抽象 `BeanReflector` 与 `FieldConvertor` 接口，使得 SearchBean 对象的反射机制更加解耦，更容易扩展与自定义
* 新增 `NumberFieldConvertor`、`StrNumFieldConvertor`、`BoolFieldConvertor` 与 `DateFormatFieldConvertor` 四个字段转换器实现，用户可以选择使用
* 新增 `DbMapping` 数据库映射接口，并提供基于下划线风格的映射实现，使得简单应用场景下，用户可以省略 `@SearchBean` 与 `@DbField` 注解
* 注解 `@SearchBean` 新增 `dataSource` 属性，用于指定该 SearchBean 从哪个数据源检索
* 注解 `@SearchBean` 新增 `autoMapTo` 属性，用于指定缺省 `@DbField` 注解的字段自动映射到那张表
* 新增 `@DbIgnore` 注解，用于指定 忽略某些字段，即添加该注解的字段不会被映射到数据库
* 注解 `@DbField` 新增 `conditional` 与 `onlyOn` 属性，使得用户可以控制该字段是否可以用作检索条件，以及当可作检索条件时支持哪些字段运算符
* 新增 `ParamAware` 接口，SearchBean 实现该接口时，可在 `afterAssembly(Map<String, Object> paraMap)` 方法里拿到原始检索参数
* 新增 onlySelect 与 selectExclude 参数（参数名可自定义），可用于指定只 Select 哪些字段，或者排除哪些字段
* 新增 `SqlInterceptor` 接口，实现 SQL 拦截器功能

#### Bean Searcher Boot Starter

* 简化使用，不再需要启动操作，不再需要配置 SearchBean 包名路径（移除了 `SearcherStarter` 类）
* Spring Boot 自动配置功能 独立到 Bean Searcher Boot Starter` 项目中，Bean Searcher 项目不再依赖 Spring

#### JDK

* 支持 JDK8+ 
* 兼容 JDK9+ 的模块引入机制

