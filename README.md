# 模块功能划分
1. core: 报表核心模块，报表相关 rest 接口、生成流程都定义在本模块。
2. web: 自定义业务前端展示模块。
3. business: 业务数据模块，用于定义各种 Entity、Mapper等。
4. report-*: 报表子模块，需要定义一个 ReportOperator 来操作生成报表，resources 目录包含 Tex 模板跟 info.yaml。


# 设计思路
使用 Thymeleaf 结合 xeLatex 进行 PDF 报表生成。

- report-* 依赖 core 跟 business。
- web 依赖 core 跟 business。
- report-* 存在于 web 的 classpath 中（用于动态添加报表）。
> 整个项目只用部署 web 模块，如果需要动态增加报表，则只用新增报表模块，打包后放到 web 模块的 classpath 中。
> 当然，也可以整体打包部署，此时需要把 report-* 作为 web 的依赖。




> 感谢 [lshort-zh-cn](https://github.com/CTeX-org/lshort-zh-cn) 项目，pdf 写的很实用也很精致。