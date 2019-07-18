package com.vinberts.shrinkly.web;

import com.vinberts.shrinkly.validation.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.Locale;

/**
 *
 */
@Configuration
@ComponentScan(basePackages = { "com.vinberts.shrinkly.web" })
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {

    @Autowired
    private MessageSource messageSource;

    public MvcConfig() {
        super();
    }

    @Override
    public void addViewControllers(final ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("home");
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/registration").setViewName("registration");
        registry.addViewController("/registrationConfirm").setViewName("registrationConfirm");
        registry.addViewController("/expiredAccount.html");
        registry.addViewController("/badUserToken").setViewName("badUserToken");
        registry.addViewController("/admin").setViewName("admin");
        registry.addViewController("/successRegister").setViewName("successRegister");
        registry.addViewController("/forgetPassword").setViewName("forgetPassword");
        registry.addViewController("/updatePassword").setViewName("updatePassword");;
        registry.addViewController("/policy").setViewName("policy");
        registry.addViewController("/privacy").setViewName("privacy");
        registry.addViewController("/reportAbuse").setViewName("reportAbuse");
        registry.addViewController("/shrinklyAnalytics").setViewName("shrinklyAnalytics");
        registry.addViewController("/user").setViewName("user");
//        registry.addStatusController("/error/404", HttpStatus.NOT_FOUND);
        registry.addStatusController("/error/403", HttpStatus.FORBIDDEN);
    }



    //    @Override
//    public void configureHandlerExceptionResolvers(final List<HandlerExceptionResolver> resolvers) {
//        SimpleMappingExceptionResolver simpleMappingExceptionResolver = new SimpleMappingExceptionResolver();
//        simpleMappingExceptionResolver.setDefaultErrorView("error/404");
//        simpleMappingExceptionResolver.setDefaultStatusCode(404);
//        resolvers.add(simpleMappingExceptionResolver);
//    }

    @Override
    public void configureDefaultServletHandling(final DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {

        // Register resource handler for CSS and JS
        registry.addResourceHandler("/resources/**").addResourceLocations("classpath:/static/");

    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        final LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("lang");
        registry.addInterceptor(localeChangeInterceptor);
    }

    @Bean
    public LocaleResolver localeResolver() {
        final CookieLocaleResolver cookieLocaleResolver = new CookieLocaleResolver();
        cookieLocaleResolver.setDefaultLocale(Locale.ENGLISH);
        return cookieLocaleResolver;
    }

    @Bean
    public EmailValidator emailValidator() {
        return new EmailValidator();
    }

    @Bean
    @ConditionalOnMissingBean(RequestContextListener.class)
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }

    @Override
    public Validator getValidator() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setValidationMessageSource(messageSource);
        return validator;
    }
}
