package com.voc.service.analysis.core.v2.events.abstracts;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.voc.service.analysis.core.v2.events.context.AnlysisEventContext;
import com.voc.service.analysis.model.AysProcessDataModel;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName AbstractEventNodeComponent
 * @createTime 2024年03月07日 13:02
 * @Copyright cuick
 * @Description: 事件节点抽象类
 */
public abstract class AbstractEventNode extends AbstractComputationRuleNode {

    private static final Logger log = LoggerFactory.getLogger(AbstractEventNode.class);
    /**
     * 事件执行权重 -》 倒序
     *
     * @return
     */
    @Setter
    @Getter
    int weight = 0;


    public static void main(String[] args) {
        String s = "{\"id\":\"022302432c0e159a9030ad2b891f1436\",\"content_type\":\"order\",\"data_create_time\":\"2025-09-25T09:56:17.000+08:00\",\"data_update_time\":\"2025-09-25T10:51:54.000+08:00\",\"create_time\":\"2025-09-25T23:11:54.045+08:00\",\"data_id\":\"022302432c0e159a9030ad2b891f1436\",\"channel_code\":\"pd_post_wb\",\"brand\":\"长安引力\",\"series\":\"启源A05真香版\",\"model\":\"C589ICA1，JL469Q1，国6b，TZ270XY551，53km，重庆-渝北工厂，电池1\",\"is_outer\":\"N\",\"mobile\":\"18704258100\",\"user_id\":\"7955935442\",\"user_name\":\"高淑娟\",\"vhl_vin\":\"LS6ANE2N3RA000666\",\"dlr_code\":\"S22331\",\"dlr_type\":\"B类维保中心\",\"title\":\"（2025.9.25客户来电撤诉，满意）【东北战区】启源 A05：反映车辆多次出现打不着火的问题，对于4S店的解答不满意，要求投诉\",\"content\":\"自动启停灯亮提示\",\"is_wsater_army\":\"N\",\"weight\":0,\"done\":0,\"model_type\":0,\"ds\":\"99999\"}\n";
        JSONObject jsonObject = JSONUtil.parseObj(s);
        System.out.println(jsonObject);
        byte[] encode = Base64.getEncoder().encode(com.alibaba.fastjson.JSONObject.toJSONString(jsonObject).getBytes(StandardCharsets.UTF_8));
        System.out.println(new String(encode));
//        String s = "eyJpZCI6IjAyMjMwMjQzMmMwZTE1OWE5MDMwYWQyYjg5MWYxNDM2IiwiY29udGVudF90eXBlIjoib3JkZXIiLCJkYXRhX2NyZWF0ZV90aW1lIjoiMjAyNS0wOS0yNVQwOTo1NjoxNy4wMDArMDg6MDAiLCJkYXRhX3VwZGF0ZV90aW1lIjoiMjAyNS0wOS0yNVQxMDo1MTo1NC4wMDArMDg6MDAiLCJjcmVhdGVfdGltZSI6IjIwMjUtMDktMjVUMjM6MTE6NTQuMDQ1KzA4OjAwIiwiZGF0YV9pZCI6IjAyMjMwMjQzMmMwZTE1OWE5MDMwYWQyYjg5MWYxNDM2IiwiY2hhbm5lbF9jb2RlIjoicGR0X29yZGVyX2NheWxyeGZ3IiwiYnJhbmQiOiLplb/lronlvJXlipsiLCJzZXJpZXMiOiLlkK/mupBBMDXnnJ/pppnniYgiLCJtb2RlbCI6IkM1ODlJQ0Ex77yMSkw0NjlRMe+8jOWbvTZi77yMVFoyNzBYWTU1Me+8jDUza23vvIzph43luoYt5rid5YyX5bel5Y6C77yM55S15rGgMSIsImlzX291dGVyIjoiTiIsIm1vYmlsZSI6IjE4NzA0MjU4MTAwIiwidXNlcl9pZCI6IjFIZWtqIiwidXNlcl9uYW1lIjoi6auY5reR5aifIiwidmhsX3ZpbiI6IkxTNkFORTJOM1JBMDAwNjY2IiwiZGxyX2NvZGUiOiJTMjIzMzEiLCJkbHJfdHlwZSI6IkLnsbvnu7Tkv53kuK3lv4MiLCJ0aXRsZSI6Iu+8iDIwMjUuOS4yNeWuouaIt+adpeeUteaSpOivie+8jOa7oeaEj++8ieOAkOS4nOWMl+aImOWMuuOAkeWQr+a6kCBBMDXvvJrlj43mmKDovabovoblpJrmrKHlh7rnjrDmiZPkuI3nnYDngavnmoTpl67popjvvIzlr7nkuo40U+W6l+eahOino+etlOS4jea7oeaEj++8jOimgeaxguaKleiviSIsImNvbnRlbnQiOiI8cD7vvIgyMDI1LjkuMjXlrqLmiLfmnaXnlLXmkqTor4nvvIzmu6HmhI/vvInjgJDmnInmirHmgKjmg4Xnu6rjgJHlj43mmKDovabovoblpJrmrKHlh7rnjrDmiZPkuI3nnYDngavnmoTpl67popjvvJvlr7nkuo40U+W6l+eahOacjeWKoeWSjOino+WGs+mXrumimOeahOiDveWKm+S4jea7oeaEj++8m+imgeaxguaKleivie+8jOeUqOaIt+ihqOekuuS5i+WJjeadpeeUteaKleivieS6hu+8jOmXqOW6l+e7j+eQhuWRiuefpeino+WGs++8jOe7meiHquW3seS4iuaKpe+8jOS9huW5tuacquS4iuaKpe+8jOS7iuWkqeaXqeS4iui9pui+huWPiOaJk+S4jeedgOeBq+S6hu+8jOmXqOW6l+e7meWIsOeahOeQhueUseW+iOeJteW8uu+8jOihqOekuuiHquW3semDveS4jeiDveaOpeWPl++8jOWRiuefpeaYr+i9pumSpeWMmeW4puedgOi/meS4quS/neaKpOWll++8jOWvvOiHtOi/meS4qui9pueahOaOpeinpuS4jeiJr++8jOiAjOS4lOajgOafpeS6huS4gOi3r+ayoeacieajgOafpeWHuuS7u+S9lemXrumimO+8jOi/mOiAveivr+iHquW3seeahOaXtumXtO+8jOaOpeinpuS4jeiJr++8jOi/meS5iOWkp+S4qumXqOW6l+WwseaYr+i/meS5iOino+WGs+mXrumimOeahOWYm++8jOihqOekuuiHquW3seS5sOS6hjXlj7Dplb/lronmsb3ovabvvIzkvYbov5nkuKrmnI3liqHoh6rlt7HmjqXlj5fkuI3kuobvvIzopoHmsYLop6PlhrPpl67popjvvIzooajnpLrmraTmipXor4nkuI3kvJrlho3mkqTplIDvvIzooajnpLrnjrDlnKjlvojkuI3mu6HmhI/vvIzlt7Loh7TmrYnvvIzlt7LlkYrnn6XorrDlvZXlj43ppojjgII8L3A+XG48cD7ovabmnrblj7c6TFM2QU5FMk4zUkEwMDA2NjY8YnI+6LSt6L2m5pe26Ze0OjIwMjUtMDMtMDM8YnI+6KGM6am26YeM56iLOjEuOOS4h+WFrOmHjDxicj7ovabniYzlj7fvvJrovr1MRkgwMDIyPGJyPuWkhOeQhumXqOW6lzrnm5jplKbkuJzmtqbmsb3ovabplIDllK7mnI3liqHmnInpmZDlhazlj7g8L3A+IiwiaXNfd3NhdGVyX2FybXkiOiJOIiwid2VpZ2h0IjowLCJkb25lIjowLCJtb2RlbF90eXBlIjowLCJkcyI6Ijk5OTk5In0=";
//        byte[] decode = Base64.getDecoder().decode(s);
//        System.out.println(new String(decode));
//        String s = "\"content\":\"<p>（2025.9.25客户来电撤诉，满意）【有抱怨情绪】反映车辆多次出现打不着火的问题；手机号18523211144 用户对于4S店的服务和解决问题的能力不满意；要求投诉，用户表示之前来电投诉了，门店经理告知解决，给自己上报，但并未上报，今天早上车辆又打不着火了，门店给到的理由很牵强，表示自己都不能接受，告知是车钥匙带着这个保护套，导致这个车的接触不良，而且检查了一路没有检查出任何问题，还耽误自己的时间，接触不良，这么大个门店就是这么解决问题的嘛，表示自己买了5台长安汽车，但这个服务自己接受不了，要求解决问题，表示此投诉不会再撤销，表示现在很不满意，已致歉，已告知记录反馈。</p>\\n<p>车架号:LS6ANE2N3RA000666<br>购车时间:2025-03-03<br>行驶里程:1.8万公里<br>车牌号：辽LFH0022<br>处理门店:盘锦东润汽车销售服务有限公司</p>\"";
//
//        String s = "140107199409021213";
//        String regex = "([冀豫云辽黑湘皖鲁苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼渝京津沪新京军空海北沈兰济南广成使领A-Z]{1}[a-zA-Z0-9]{5,6}[a-zA-Z0-9挂学警港澳]{1})";
//        Pattern pattern = Pattern.compile(regex);
//        Matcher matcher = pattern.matcher(s);

//        if (matcher.find()) {
//            System.out.println("包含身份证号: " + matcher.group());
//        } else {
//            System.out.println("不包含身份证号");
//        }
    }

    @Override
    public boolean isAccess() {
        super.isAccess();
        AnlysisEventContext context = this.getRequestData();
        //过滤掉的数据不执行后续规则
        if ("1".equalsIgnoreCase(context.getFinshData().getAbandon())) {
            return false;
        }
        //规则中配置的内容属性过滤
        final boolean isAccess_ = this.ruleContentFilter(context.getFinshData());
        if (!isAccess_) {
            log.warn("本条内容未满足[规则中配置的内容属性]配置，跳过");
            return false;
        }
        //条件关系判断
        boolean isAccess =false;
        try {
            isAccess = this.relationshipProcess(context.getClientId(), this.getComputLogicModel());
        } catch (Exception e) {
            log.error("msg: {}, 原始数据：{}", e.getMessage(), JSONUtil.toJsonStr(this.getComputLogicModel()));
            throw e;
        }
//        context.getStopWatch().stop();
        if (isAccess) {
            log.info("命中规则 ruleId:{} ,ruleName:{} , contentId:{}",
                    this.getComputLogicModel().getRuleId(),
                    this.getComputLogicModel().getRuleName()
                    , context.getFinshData().getDataId());
        } else {
            log.info("未命中规则 ruleId:{} ,ruleName:{} , contentId:{}", this.getComputLogicModel().getRuleId(), this.getComputLogicModel().getRuleName()
                    , context.getFinshData().getDataId());
        }
        if (CollUtil.isNotEmpty(conditionResult) && log.isTraceEnabled()) {
            conditionResult.stream().filter(ObjectUtil::isNotNull).forEach(e -> {
                log.trace("条件执行结果：{}", JSONUtil.toJsonStr(e));
            });
        } else {
            log.warn("无条件执行结果");
        }

        return isAccess;
    }


    /**
     * 规则中配置的内容属性过滤
     * 渠道、内容类型、数据来源等
     *
     * @param finshData
     * @return
     */
    private boolean ruleContentFilter(final AysProcessDataModel finshData) {
        List<Boolean> all = new ArrayList<>();
        if (this.getComputLogicModel().getChannelIds().contains("all")) {
            all.add(true);
        } else {
//            all.add(this.getComputLogicModel().getContentType().equals(finshData.getContentType()));
            all.add(this.getComputLogicModel().getChannelIds().contains(finshData.getChannelId()));
        }
        return all.stream().allMatch(rs -> rs);
    }

    @Override
    public void beforeProcess() {
        super.beforeProcess();
//        AnlysisEventContext context = this.getRequestData();
//        //记录本条内容命中的规则
//        context.getFinshData().getHitRuleList().add(RuleModel.builder()
//                .eventCode(this.getComputLogicModel().getEventCode())
//                .id(this.getComputLogicModel().getRuleId())
//                .title(this.getComputLogicModel().getRuleName())
//                .build());
    }

    @Override
    public void afterProcess() {
        super.afterProcess();
    }


    @Override
    public void onError(Exception e) throws Exception {
        super.onError(e);
        AnlysisEventContext context = this.getRequestData();
        log.error("{} 异常 {} ,原始数据：", this.getName(), e.getMessage(), context.getFinshData());
        this.getSlot().setException(e);
        //是否结束整个流程
        super.setIsEnd(true);
    }

    /**
     * 内容处理逻辑
     */
    @Override
    public void process() throws Exception {
        log.info("{}", this.getClass().getSimpleName());
        AnlysisEventContext context = this.getRequestData();
        log.trace("内容[{}]前: {}", this.getName(), this.getContentItem());
        this.action(context.getClientId(), this.getComputLogicModel());
        log.trace("内容[{}]后: {}", this.getName(), this.getContentItem());
        log.info("完成内容[{}]事件", this.getName());
    }
}
