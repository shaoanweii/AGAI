//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.alicp.jetcache.anno.method;

import com.alicp.jetcache.CacheConfigException;
import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionEvaluator implements Function<Object, Object> {
    private static final Pattern pattern = Pattern.compile("\\s*(\\w+)\\s*\\{(.+)\\}\\s*");
    private Function<Object, Object> target;

    public ExpressionEvaluator(String script, Method defineMethod) {
        Object[] rt = this.parseEL(script);
        EL el = (EL)rt[0];
        String realScript = (String)rt[1];
        if (el == EL.MVEL) {
            this.target = new MvelEvaluator(realScript);
        } else if (el == EL.SPRING_EL) {
            this.target = new MySpelEvaluator(realScript, defineMethod);
        }

    }

    private Object[] parseEL(String script) {
        if (script != null && !"".equals(script.trim())) {
            Object[] rt = new Object[2];
            Matcher matcher = pattern.matcher(script);
            if (!matcher.matches()) {
                rt[0] = EL.SPRING_EL;
                rt[1] = script;
                return rt;
            } else {
                String s = matcher.group(1);
                if ("spel".equals(s)) {
                    rt[0] = EL.SPRING_EL;
                } else {
                    if (!"mvel".equals(s)) {
                        throw new CacheConfigException("Can't parse \"" + script + "\"");
                    }

                    rt[0] = EL.MVEL;
                }

                rt[1] = matcher.group(2);
                return rt;
            }
        } else {
            return null;
        }
    }

    @Override
    public Object apply(Object o) {
        return this.target.apply(o);
    }

    Function<Object, Object> getTarget() {
        return this.target;
    }
}
