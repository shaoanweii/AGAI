//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.alicp.jetcache.anno.method;

import cn.hutool.core.util.StrUtil;
import com.voc.service.common.util.ServiceContextHolder;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.function.Function;

class MySpelEvaluator implements Function<Object, Object> {
    private static ExpressionParser parser = new SpelExpressionParser();
    private static ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    private final Expression expression;
    private String[] parameterNames;

    public MySpelEvaluator(String script, Method defineMethod) {
        this.expression = parser.parseExpression(script);
        if (defineMethod.getParameterCount() > 0) {
            this.parameterNames = parameterNameDiscoverer.getParameterNames(defineMethod);
        }
    }

    @Override
    public Object apply(Object rootObject) {
        EvaluationContext context = new StandardEvaluationContext(rootObject);
        CacheInvokeContext cic = (CacheInvokeContext) rootObject;
        //设置系统名 :appId:[name]
        /*log.debug("appId:{}", ServiceContextHolder.getAppId());
        if (!StrUtil.startWith(cic.getCacheInvokeConfig().getCachedAnnoConfig().getName(), ":".concat(ServiceContextHolder.getSystemId()))) {
            cic.getCacheInvokeConfig().getCachedAnnoConfig().setName(
                    StrUtil.format(":{}:{}", ServiceContextHolder.getSystemId(), cic.getCacheInvokeConfig().getCachedAnnoConfig().getName()));
        }*/
        if (this.parameterNames != null) {
            for (int i = 0; i < this.parameterNames.length; ++i) {
                context.setVariable(this.parameterNames[i], cic.getArgs()[i]);
            }
        }

        context.setVariable("result", cic.getResult());
        Object elStr = this.expression.getValue(context);
        String generateKey = this.generateKey(String.valueOf(elStr));
        return generateKey;
    }

    private String generateKey(String key) {
        if (StrUtil.isBlank(key) || !key.contains("C{")) {
//            return PrefixRedisSerializer.DEFUAL_PREFIX.concat(key);
            return key;
        }
        if (key.contains("C{userId}")) {
            final String userId = ServiceContextHolder.getUserId();
            if (StrUtil.isBlank(userId)) {
                return null;
            }
            key = key.replace("C{userId}", userId);
        }
        if (key.contains("C{token}")) {
            final String token = ServiceContextHolder.getUser().getTokenKey();
            if (StrUtil.isBlank(token)) {
                return null;
            }
            key = key.replace("C{token}", token);
        }
        if (key.contains("C{appId}")) {
            final String token = ServiceContextHolder.getSystemId();
            if (StrUtil.isBlank(token)) {
                return null;
            }
            key = key.replace("C{appId}", token);
        }
        if (key.contains("C{clientId}")) {
            final String userId = ServiceContextHolder.getUserId();
            if (StrUtil.isBlank(userId)) {
                return null;
            }
            key = key.replace("C{clientId}", userId);
        }
        //加前缀
//        return PrefixRedisSerializer.DEFUAL_PREFIX.concat(key);
        return key;
    }
}
