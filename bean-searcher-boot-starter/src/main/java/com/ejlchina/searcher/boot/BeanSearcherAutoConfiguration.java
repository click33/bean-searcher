package com.ejlchina.searcher.boot;

import com.ejlchina.searcher.*;
import com.ejlchina.searcher.boot.BeanSearcherProperties.ParamsProps;
import com.ejlchina.searcher.boot.BeanSearcherProperties.SqlProps;
import com.ejlchina.searcher.dialect.Dialect;
import com.ejlchina.searcher.dialect.MySqlDialect;
import com.ejlchina.searcher.dialect.OracleDialect;
import com.ejlchina.searcher.implement.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.List;


@Configuration
@ConditionalOnBean(DataSource.class)
@AutoConfigureAfter({ DataSourceAutoConfiguration.class })
@EnableConfigurationProperties(BeanSearcherProperties.class)
public class BeanSearcherAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(PageExtractor.class)
	public PageExtractor pageExtractor(BeanSearcherProperties config) {
		ParamsProps.PaginationProps conf = config.getParams().getPagination();
		String type = conf.getType();
		BasePageExtractor extractor;
		if (ParamsProps.PaginationProps.TYPE_PAGE.equals(type)) {
			PageSizeExtractor p = new PageSizeExtractor();
			p.setPageName(conf.getPage());
			extractor = p;
		}  else
		if (ParamsProps.PaginationProps.TYPE_OFFSET.equals(type)) {
			PageOffsetExtractor p = new PageOffsetExtractor();
			p.setOffsetName(conf.getOffset());
			extractor = p;
		} else {
			throw new SearchException("配置项 [bean-searcher.params.pagination.type] 只能为 page 或 offset！");
		}
		extractor.setMaxAllowedSize(conf.getMaxAllowedSize());
		extractor.setSizeName(conf.getSize());
		extractor.setStart(conf.getStart());
		extractor.setDefaultSize(conf.getDefaultSize());
		return extractor;
	}

	@Bean
	@ConditionalOnMissingBean(ParamResolver.class)
	public ParamResolver paramResolver(PageExtractor pageExtractor,
									   ObjectProvider<ParamFilter[]> paramFilters,
									   BeanSearcherProperties config) {
		DefaultParamResolver paramResolver = new DefaultParamResolver();
		paramResolver.setPageExtractor(pageExtractor);
		paramFilters.ifAvailable(paramResolver::setParamFilters);
		ParamsProps conf = config.getParams();
		paramResolver.setOperatorSuffix(conf.getOperatorKey());
		paramResolver.setIgnoreCaseSuffix(conf.getIgnoreCaseKey());
		paramResolver.setOrderName(conf.getOrder());
		paramResolver.setSortName(conf.getSort());
		paramResolver.setSeparator(conf.getSeparator());
		paramResolver.setOnlySelectName(conf.getOnlySelect());
		paramResolver.setSelectExcludeName(conf.getSelectExclude());
		return paramResolver;
	}

	@Bean
	@ConditionalOnMissingBean(Dialect.class)
	public Dialect dialect(BeanSearcherProperties config) {
		String dialect = config.getSql().getDialect();
		if (dialect == null) {
			throw new SearchException("配置项【bean-searcher.sql.dialect】不能为空");
		}
		switch (dialect.toLowerCase()) {
		case SqlProps.DIALECT_MYSQL:
			return new MySqlDialect();
		case SqlProps.DIALECT_ORACLE:
			return new OracleDialect();
		}
		throw new SearchException("配置项【bean-searcher.sql.dialect】只能为  MySql | Oracle 中的一个，若需支持其它方言，可自己注入一个 com.ejlchina.searcher.dialect.Dialect 类型的 Bean！");
	}
	
	@Bean
	@ConditionalOnMissingBean(DateValueCorrector.class)
	public DateValueCorrector dateValueCorrector() {
		return new DateValueCorrector();
	}

	@Bean
	@ConditionalOnMissingBean(SqlResolver.class)
	public SqlResolver sqlResolver(Dialect dialect, DateValueCorrector dateValueCorrector) {
		return new DefaultSqlResolver(dialect, dateValueCorrector);
	}

	@Bean
	@ConditionalOnMissingBean(SqlExecutor.class)
	public SqlExecutor sqlExecutor(DataSource dataSource) {
		return new DefaultSqlExecutor(dataSource);
	}
	
	@Bean
	@ConditionalOnMissingBean(BeanReflector.class)
	public BeanReflector beanReflector(ObjectProvider<List<FieldConvertor>> convertorsProvider) {
		List<FieldConvertor> convertors = convertorsProvider.getIfAvailable();
		if (convertors != null) {
			return new DefaultBeanReflector(convertors);
		}
		return new DefaultBeanReflector();
	}

	@Bean
	@ConditionalOnMissingBean(MetaResolver.class)
	public MetaResolver metaResolver(ObjectProvider<SnippetResolver> snippetResolver, ObjectProvider<DbMapping> dbMapping) {
		DefaultMetaResolver metaResolver = new DefaultMetaResolver();
		snippetResolver.ifAvailable(metaResolver::setSnippetResolver);
		dbMapping.ifAvailable(metaResolver::setDbMapping);
		return metaResolver;
	}

	@Bean
	@ConditionalOnMissingBean(BeanSearcher.class)
	public BeanSearcher beanSearcher(MetaResolver metaResolver,
									 ParamResolver paramResolver,
									 SqlResolver sqlResolver,
									 SqlExecutor sqlExecutor,
									 BeanReflector beanReflector,
									 ObjectProvider<List<SqlInterceptor>> interceptors) {
		DefaultBeanSearcher searcher = new DefaultBeanSearcher();
		searcher.setMetaResolver(metaResolver);
		searcher.setParamResolver(paramResolver);
		searcher.setSqlResolver(sqlResolver);
		searcher.setSqlExecutor(sqlExecutor);
		searcher.setBeanReflector(beanReflector);
		interceptors.ifAvailable(searcher::setInterceptors);
		return searcher;
	}

	@Bean
	@ConditionalOnMissingBean(MapSearcher.class)
	public MapSearcher mapSearcher(MetaResolver metaResolver,
								   ParamResolver paramResolver,
								   SqlResolver sqlResolver,
								   SqlExecutor sqlExecutor,
								   ObjectProvider<List<SqlInterceptor>> interceptors,
								   ObjectProvider<List<FieldConvertor>> convertors) {
		DefaultMapSearcher searcher = new DefaultMapSearcher();
		searcher.setMetaResolver(metaResolver);
		searcher.setParamResolver(paramResolver);
		searcher.setSqlResolver(sqlResolver);
		searcher.setSqlExecutor(sqlExecutor);
		interceptors.ifAvailable(searcher::setInterceptors);
		convertors.ifAvailable(searcher::setConvertors);
		return searcher;
	}

}
