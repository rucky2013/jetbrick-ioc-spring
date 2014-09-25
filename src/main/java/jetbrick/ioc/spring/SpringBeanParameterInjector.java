/**
 * Copyright 2013-2014 Guoqiang Chen, Shanghai, China. All rights reserved.
 *
 * Email: subchen@gmail.com
 * URL: http://subchen.github.io/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrick.ioc.spring;

import java.lang.annotation.Annotation;
import javax.servlet.ServletContext;
import jetbrick.ioc.Ioc;
import jetbrick.ioc.injector.ParameterInjector;
import jetbrick.util.Validate;
import jetbrick.util.annotation.ValueConstants;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

//注入 @SpringBean 标注的参数
public final class SpringBeanParameterInjector implements ParameterInjector {
    private ApplicationContext appctx;
    private String name;
    private boolean required;

    @Override
    public void initialize(ParameterContext ctx) {
        Annotation annotation = ctx.getAnnotation();
        Validate.isInstanceOf(SpringBean.class, annotation);

        SpringBean inject = (SpringBean) annotation;
        this.appctx = getApplicationContext(ctx.getIoc());
        this.name = ValueConstants.defaultValue(inject.value(), ctx.getParameterName()); // 默认取参数名称
        this.required = inject.required();
    }

    private ApplicationContext getApplicationContext(Ioc ioc) {
        ApplicationContext ctx = ioc.getBean(ApplicationContext.class);
        if (ctx != null) {
            return ctx;
        }

        ServletContext sc = ioc.getBean(ServletContext.class);
        if (sc != null) {
            ctx = WebApplicationContextUtils.getWebApplicationContext(sc);
        }

        if (ctx == null) {
            throw new IllegalStateException("No Spring Container Found!");
        }

        return ctx;
    }

    @Override
    public Object getObject() {
        Object value = appctx.getBean(name);
        if (value == null && required) {
            throw new IllegalStateException("Can't inject @SpringBean parameter: " + name);
        }
        return value;
    }
}
