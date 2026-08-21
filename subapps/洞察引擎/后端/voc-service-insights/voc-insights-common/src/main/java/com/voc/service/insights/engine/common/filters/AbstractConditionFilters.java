package com.voc.service.insights.engine.common.filters;


import cn.hutool.core.util.StrUtil;
import com.alibaba.ttl.TtlWrappers;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.common.util.StopWatch;
import com.voc.service.insights.engine.api.*;
import com.voc.service.insights.engine.api.constants.*;
import com.voc.service.insights.engine.enums.*;
import com.voc.service.insights.engine.model.InsAutomarkModel;
import com.voc.service.insights.engine.model.InsBrandInfoModel;
import com.voc.service.insights.engine.model.InsChannelInfoModel;
import com.voc.service.insights.engine.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName AbstractConditionFilters
 * @createTime 2024年02月21日 9:29
 * @Copyright futong
 */
@Component
public abstract class AbstractConditionFilters implements IConditionFilters {
    private static final Logger log = LoggerFactory.getLogger(AbstractConditionFilters.class);
    @Autowired
    IInsBasicInfoService basicInfoService;
    @Autowired
    IInsDictService dictService;
    @Autowired
    IInsChannelInfoService channelInfoService;
    @Autowired
    IInsBrandInfoService brandInfoService;
    @Autowired
    IInsCustomerInfoService customerInfoService;
    @Autowired
    IInsTagLibService iInsTagLibService;
    @Autowired
    IInsCarSeriesInfoService carSeriesInfoService;
    @Autowired
    private IInsAutomarkService automarkService;




    @Override
    public ConditionVo get(String key) {
        ConditionVo conditionVo = null;
        switch (key) {
            case PROVINCE:
                conditionVo = getProvince();
                break;
            case BRAND:
                conditionVo = getBrandInfo();
                break;
            case ENERGY:
                conditionVo = getEnergyType();
                break;
            case APP_CLIENT:
                conditionVo = getAppClient();
                break;
            case CAR_TYPE:
                conditionVo = getCarType();
                break;
            case REGULATION_PRE_TYPE:
                conditionVo = getRegulationPreType();
                break;
            case REGULATION_POST_TYPE:
                conditionVo = getRegulationPostType();
                break;
            case REGULATION_CONTENT_TYPE:
                conditionVo = getRegulationContentType();
                break;
            case REGULATION_STATUS_TYPE:
                conditionVo = getRegulationStatusType();
                break;
            case REGULATION_STAGE:
                conditionVo = getRegulationStage();
                break;
            case REGULATION_RELATIONS:
                conditionVo = getRegulationRelations();
                break;
            case REGULATION_CLASSIFY:
                conditionVo = getRegulationClassify();
                break;
            case RULE_WEIGHT:
                conditionVo = getRuleWeight();
                break;
            case RULE_CONDITION_TYPE:
                conditionVo = getRuleConditionType();
                break;
            case RULE_LOGICAL_OPERATOR:
                conditionVo = getRuleLogicalOperator();
                break;
            case VARIABLE_VALUE:
                conditionVo = getRuleVariableValue();
                break;
            case RESOURCE_GROUP_TYPE:
                conditionVo = getResourceGroupType();
                break;
            case TAG_LIB_ATTRIBUTE:
                conditionVo = getTagLibAttribute();
                break;
            case CATEGORY_TYPE:
                conditionVo = getCategoryType();
                break;
            case INCREASE_TYPE:
                conditionVo = getIncreaseType();
                break;
            case ENABLE_STATUS:
                conditionVo = getEnableStatus();
                break;
            case AUDIT_STATUS:
                conditionVo = getAuditStatus();
                break;
            case ACCOUNT_STATUS:
                conditionVo = getAccountStatus();
                break;
            case BRAND_CAR:
                conditionVo = getBrandCarsTree();
                break;
            case LABEL_AND_MODEL:
                conditionVo = getLabelAndModel();
                break;
            case SELF_BRAND:
                conditionVo = getSelfBrand();
                break;
            case SELF_BRAND_CAR:
                conditionVo = getSelfBrandCarTree();
                break;
            case AUTOMARK:
                conditionVo = getAutoMark();
                break;
            case CUSTOMER_GENDER:
                conditionVo = getCustomerGender();
                break;
            case WATER_MAN:
                conditionVo = getWaterMan();
                break;
            case V_MAN:
                conditionVo = getVMan();
                break;
            case CAR_OWNER:
                conditionVo = getCarOwner();
                break;
            case INDICATOR_EFFECT_RELATION:
                conditionVo = getIndicatorEffectRelation();
                break;
            default:
                conditionVo = getDict(key);
                break;
        }
        return conditionVo;
    }
    /**
     * @return com.voc.service.insights.engine.vo.ConditionVo
     * @描述 获取指标生效关系
     **/
    protected ConditionVo getIndicatorEffectRelation() {
        List<ConditionDetailsVo> vo = new ArrayList<>();
        vo.add(ConditionDetailsVo.builder().key("AND").value("满足全部条件生效").build());
        vo.add(ConditionDetailsVo.builder().key("OR").value("满足任一条件生效").build());
        ConditionVo conditionVo = new ConditionVo(StrUtil.toCamelCase(INDICATOR_EFFECT_RELATION), vo);
        return conditionVo;
    }

    protected ConditionVo getDict(String key) {
        List<DictInfoVo> dictInfoByCode = dictService.findDictInfoByCode(key);
        if (ObjectUtils.isEmpty(dictInfoByCode)) {
            return null;
        }
        Map<String, List<DictInfoVo>> collect = dictInfoByCode.stream().collect(Collectors.groupingBy(DictInfoVo::getTypeCode, LinkedHashMap::new, Collectors.toList()));
        List<ConditionDetailsVo> dict = collect.entrySet().stream().map(e -> {
            final List<DictInfoVo> value = e.getValue();
            final DictInfoVo dictInfoVo = value.stream().findFirst().get();
            ConditionDetailsVo build = ConditionDetailsVo.builder().key(dictInfoVo.getTypeCode()).value(dictInfoVo.getTypeName()).build();
            if(ObjectUtils.isNotEmpty(dictInfoVo.getClassifyCode())){
                List<ConditionDetailsVo> collect1 = value.stream().map(v -> ConditionDetailsVo.builder().key(v.getClassifyCode()).value(v.getClassifyName()).build()).collect(Collectors.toList());
                build.setChildren(collect1);
            }
            return build;
        }).collect(Collectors.toList());
        return new ConditionVo(StrUtil.toCamelCase(key), dict);
    }


    /**
     * @return com.voc.service.insights.engine.vo.ConditionVo
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/21 13:36
     * @描述 获取省市查询条件
     **/
    protected ConditionVo getProvince() {
        //获取全部省市信息
        List<ConditionDetailsVo> province;
        List<ProvinceAreaVo> allArea = basicInfoService.findAll();
        if (ObjectUtils.isEmpty(allArea)) {
            return null;
        }
        Map<String, List<ProvinceAreaVo>> collect = allArea.stream().collect(Collectors.groupingBy(ProvinceAreaVo::getBigAreaSale));
        province = collect.entrySet().stream().map(e -> {
            final String key = e.getKey();
            final List<ProvinceAreaVo> value = e.getValue();
            Map<String, List<ProvinceAreaVo>> collect2 = value.stream().filter(k -> ObjectUtils.isNotEmpty(k.getSmallAreaSale())).collect(Collectors.groupingBy(k -> k.getSmallAreaSale()));
            List<ConditionDetailsVo> collect1 = collect2.entrySet().stream().map(k -> {
                final String smallAreaName = k.getKey();
                final List<ProvinceAreaVo> smallAreaNameList = k.getValue();
                Map<String, List<ProvinceAreaVo>> provinceMap = smallAreaNameList.stream().filter(v -> ObjectUtils.isNotEmpty(v.getProvinceCode())).collect(Collectors.groupingBy(v -> v.getProvinceCode()));
                Map<String, ProvinceAreaVo> collect5 = smallAreaNameList.stream().filter(v -> ObjectUtils.isNotEmpty(v.getProvinceCode())).collect(Collectors.toMap(v -> v.getProvinceCode(), v -> v, (k1, k2) -> k2));
                List<ConditionDetailsVo> collect3 = provinceMap.entrySet().stream().map(v -> {
                    final String provinceCode = v.getKey();
                    List<ProvinceAreaVo> provinceList = v.getValue();
                    Map<String, ProvinceAreaVo> cityCodeMap = provinceList.stream().filter(l -> ObjectUtils.isNotEmpty(l.getAreaCode())).collect(Collectors.toMap(l -> l.getAreaCode(), l -> l, (k1, k2) -> k2));
                    List<ConditionDetailsVo> collect4 = cityCodeMap.entrySet().stream().map(l -> {
                        return ConditionDetailsVo.builder().key(l.getValue().getAreaCode()).value(l.getValue().getAreaName()).build();
                    }).collect(Collectors.toList());
                    return ConditionDetailsVo.builder().value(collect5.get(provinceCode).getProvinceName()).key(provinceCode).children(collect4).build();
                }).collect(Collectors.toList());
                return ConditionDetailsVo.builder().key(smallAreaName).value(smallAreaName).children(collect3).build();
            }).collect(Collectors.toList());
            return ConditionDetailsVo.builder().key(key).value(key).children(collect1).build();
        }).collect(Collectors.toList());
        ConditionVo conditionVo = new ConditionVo(StrUtil.toCamelCase(PROVINCE), province);
        return conditionVo;
    }

    protected ConditionVo getBrandInfo() {
        List<InsBrandInfoModel> all = brandInfoService.findAll();
        List<ConditionDetailsVo> res = new ArrayList<>();
        all.stream().forEach(e -> {
            ConditionDetailsVo build = ConditionDetailsVo.builder().code(e.getId()).key(e.getCode()).value(e.getName()).build();
            res.add(build);
        });
        return new ConditionVo(StrUtil.toCamelCase(BRAND), res);
    }

    protected ConditionVo getBrandCarsTree() {
        List<BrandInfoVo> brandCarsTree = carSeriesInfoService.findBrandCarsTree();
        List<ConditionDetailsVo> res = new ArrayList<>();
        brandCarsTree.stream().forEach(e -> {
            ConditionDetailsVo build = ConditionDetailsVo.builder().key(e.getCode()).value(e.getName()).build();
            if (ObjectUtils.isNotEmpty(e.getCars())) {
                final List<CarInfoVo> cars = e.getCars();
                List<ConditionDetailsVo> child = new ArrayList<>();
                cars.stream().forEach(k -> {
                    ConditionDetailsVo car = ConditionDetailsVo.builder().key(k.getCode()).value(k.getName()).build();
                    child.add(car);
                });
                build.setChildren(child);
            }
            res.add(build);
        });
        return new ConditionVo(StrUtil.toCamelCase(BRAND_CAR), res);
    }

    protected ConditionVo getSelfBrand() {
        List<InsBrandInfoModel> all = brandInfoService.findSelfBrand();
        List<ConditionDetailsVo> res = new ArrayList<>();
        all.stream().forEach(e -> {
            ConditionDetailsVo build = ConditionDetailsVo.builder().code(e.getId()).key(e.getCode()).value(e.getName()).build();
            res.add(build);
        });
        return new ConditionVo(StrUtil.toCamelCase(SELF_BRAND), res);
    }

    protected ConditionVo getSelfBrandCarTree() {
        List<BrandInfoVo> brandCarsTree = carSeriesInfoService.findSelfBrandCarsTree();
        List<ConditionDetailsVo> res = new ArrayList<>();
        brandCarsTree.stream().forEach(e -> {
            ConditionDetailsVo build = ConditionDetailsVo.builder().key(e.getCode()).value(e.getName()).build();
            if (ObjectUtils.isNotEmpty(e.getCars())) {
                final List<CarInfoVo> cars = e.getCars();
                List<ConditionDetailsVo> child = new ArrayList<>();
                cars.stream().forEach(k -> {
                    ConditionDetailsVo car = ConditionDetailsVo.builder().key(k.getCode()).value(k.getName()).build();
                    child.add(car);
                });
                build.setChildren(child);
            }
            res.add(build);
        });
        return new ConditionVo(StrUtil.toCamelCase(SELF_BRAND_CAR), res);
    }

    protected ConditionVo getLabelAndModel() {
        List<LabelAndModelVo> labelAndModel = basicInfoService.findLabelAndModel();
        if (ObjectUtils.isEmpty(labelAndModel)) {
            return null;
        }

        List<ConditionDetailsVo> res = new ArrayList<>();
        LinkedHashMap<String, List<LabelAndModelVo>> collect = labelAndModel.stream().collect(Collectors.groupingBy(LabelAndModelVo::getTypeCode, LinkedHashMap::new, Collectors.toList()));
        collect.entrySet().stream().forEach(e -> {
            final List<LabelAndModelVo> value = e.getValue();
            LabelAndModelVo labelAndModelVo = value.stream().findFirst().get();
            ConditionDetailsVo build = ConditionDetailsVo.builder().key(labelAndModelVo.getTypeCode()).value(labelAndModelVo.getTypeName()).build();
            List<ConditionDetailsVo> child = new ArrayList<>();
            value.stream().forEach(k -> {
                ConditionDetailsVo model = ConditionDetailsVo.builder().key(k.getClassifyCode()).value(k.getProcessingModel()).build();
                child.add(model);
            });
            build.setChildren(child);
            res.add(build);
        });
        return new ConditionVo(StrUtil.toCamelCase(LABEL_AND_MODEL), res);
    }


    protected ConditionVo getCarType() {
        List<CarTypeVo> energyInfo = basicInfoService.findCarType();
        if (ObjectUtils.isEmpty(energyInfo)) {
            return null;
        }
        List<ConditionDetailsVo> vehicle = new ArrayList<>();
        Map<String, List<CarTypeVo>> collect = energyInfo.stream().collect(Collectors.groupingBy(CarTypeVo::getTypeCode));
        collect.entrySet().stream().forEach(e -> {
            final String key = e.getKey();
            final List<CarTypeVo> value = e.getValue();
            CarTypeVo energyInfoVo = value.stream().findFirst().get();
            ConditionDetailsVo build = ConditionDetailsVo.builder().key(key).value(energyInfoVo.getTypeName()).build();
            List<ConditionDetailsVo> child = new ArrayList<>();
            value.stream().forEach(k -> {
                ConditionDetailsVo energy = ConditionDetailsVo.builder().key(k.getClassifyCode()).value(k.getClassifyName()).build();
                if (StrUtil.isNotEmpty(k.getClassifyCode()) && StrUtil.isNotEmpty(k.getClassifyName())) {
                    child.add(energy);
                }
            });
            if (ObjectUtils.isNotEmpty(child)) {
                build.setChildren(child);
            }
            vehicle.add(build);
        });
        ConditionVo conditionVo = new ConditionVo(StrUtil.toCamelCase(CAR_TYPE), vehicle);
        return conditionVo;
    }

    /**
     * @return com.voc.service.insights.engine.vo.ConditionVo
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/21 13:45
     * @描述 获取能源类型查询条件
     **/
    protected ConditionVo getEnergyType() {
        List<EnergyInfoVo> energyInfo = basicInfoService.findAllEnergyInfo();
        if (ObjectUtils.isEmpty(energyInfo)) {
            return null;
        }
        List<ConditionDetailsVo> vehicle = new ArrayList<>();
        Map<String, List<EnergyInfoVo>> collect = energyInfo.stream().collect(Collectors.groupingBy(EnergyInfoVo::getTypeCode));
        collect.entrySet().stream().forEach(e -> {
            final String key = e.getKey();
            final List<EnergyInfoVo> value = e.getValue();
            EnergyInfoVo energyInfoVo = value.stream().findFirst().get();
            ConditionDetailsVo build = ConditionDetailsVo.builder().key(key).value(energyInfoVo.getTypeName()).build();
            List<ConditionDetailsVo> child = new ArrayList<>();
            value.stream().forEach(k -> {
                ConditionDetailsVo energy = ConditionDetailsVo.builder().key(k.getClassifyCode()).value(k.getClassifyName()).build();
                if (StrUtil.isNotEmpty(k.getClassifyCode()) && StrUtil.isNotEmpty(k.getClassifyName())) {
                    child.add(energy);
                }
            });
            if (ObjectUtils.isNotEmpty(child)) {
                build.setChildren(child);
            }
            vehicle.add(build);
        });
        ConditionVo conditionVo = new ConditionVo(StrUtil.toCamelCase(ENERGY), vehicle);
        return conditionVo;
    }

    protected ConditionVo getAppClient() {
        List<CustomerInfoVo> vo = customerInfoService.findAllCustomerInfo();
        if (ObjectUtils.isEmpty(vo)) {
            return null;
        }
        List<ConditionDetailsVo> res = new ArrayList<>();

        final boolean admin = ServiceContextHolder.isAdmin();
        if (admin) {
            //管理员
            vo.stream().sorted(Comparator.comparing(e -> e.getSort(), Comparator.nullsLast(Comparator.naturalOrder()))).forEach(e -> {
                ConditionDetailsVo build = ConditionDetailsVo.builder().key(e.getId()).value(e.getAbbreviation()).build();
                if ("system".equalsIgnoreCase(e.getCode())) {
                    build.setValue("标准");
                }
                res.add(build);
            });
        } else {
            //非管理员
            final String clientId = ServiceContextHolder.getClientId();
            if (StrUtil.isNotBlank(clientId)) {
                vo.stream().filter(e -> clientId.equalsIgnoreCase(e.getId())).forEach(e -> {
                    ConditionDetailsVo build = ConditionDetailsVo.builder().key(e.getId()).value(e.getAbbreviation()).build();
                    res.add(build);
                });
            }
        }
        ConditionVo conditionVo = new ConditionVo(StrUtil.toCamelCase(APP_CLIENT), res);
        return conditionVo;
    }

    /**
     * @return com.voc.service.insights.engine.vo.ConditionVo
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/4/12 14:07
     * @描述 规则前置类型
     **/
    protected ConditionVo getRegulationPreType() {
        List<ConditionDetailsVo> vo = new ArrayList<>();
        Arrays.stream(RuleActionConditionType.values()).filter(e -> RuleActionConditionType.FilterDistinct.getCode().equalsIgnoreCase(e.getCode()) || RuleActionConditionType.Clean.getCode().equals(e.getCode())).forEach(e -> {
            ConditionDetailsVo build = ConditionDetailsVo.builder().key(e.getCode()).value(e.getText()).build();
            vo.add(build);
        });
        ConditionVo conditionVo = new ConditionVo(StrUtil.toCamelCase(REGULATION_PRE_TYPE), vo);
        return conditionVo;
    }

    /**
     * @return com.voc.service.insights.engine.vo.ConditionVo
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/4/12 14:07
     * @描述 规则后置类型
     **/
    protected ConditionVo getRegulationPostType() {
        List<ConditionDetailsVo> vo = new ArrayList<>();
        Arrays.stream(RuleActionConditionType.values())
                .filter(e -> !RuleActionConditionType.Desensitization.getCode().equalsIgnoreCase(e.getCode()))
                .filter(e -> !RuleActionConditionType.Clean.getCode().equalsIgnoreCase(e.getCode()))
                .forEach(e -> {
                    if (!e.getCode().equalsIgnoreCase(RuleActionConditionType.Desensitization.getCode())) {
                        ConditionDetailsVo build = ConditionDetailsVo.builder().key(e.getCode()).value(e.getText()).build();
                        vo.add(build);
                    }

                });
        ConditionVo conditionVo = new ConditionVo(StrUtil.toCamelCase(REGULATION_POST_TYPE), vo);
        return conditionVo;
    }

    /**
     * @return com.voc.service.insights.engine.vo.ConditionVo
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/4/12 14:07
     * @描述 规则内容格式
     **/
    protected ConditionVo getRegulationContentType() {
        List<ConditionDetailsVo> vo = new ArrayList<>();
        Arrays.stream(RuleContentType.values()).forEach(e -> {
            ConditionDetailsVo build = ConditionDetailsVo.builder().key(e.getCode()).value(e.getText()).build();
            vo.add(build);
        });
        ConditionVo conditionVo = new ConditionVo(StrUtil.toCamelCase(REGULATION_CONTENT_TYPE), vo);
        return conditionVo;
    }

    /**
     * @return com.voc.service.insights.engine.vo.ConditionVo
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/4/12 14:07
     * @描述 规则状态
     **/
    protected ConditionVo getRegulationStatusType() {
        List<ConditionDetailsVo> vo = new ArrayList<>();
        Arrays.stream(RuleStatusType.values()).forEach(e -> {
            ConditionDetailsVo build = ConditionDetailsVo.builder().key(e.getCode()).value(e.getText()).build();
            vo.add(build);
        });
        ConditionVo conditionVo = new ConditionVo(StrUtil.toCamelCase(REGULATION_STATUS_TYPE), vo);
        return conditionVo;
    }

    /**
     * @return com.voc.service.insights.engine.vo.ConditionVo
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/4/12 14:06
     * @描述 规则处理阶段
     **/
    protected ConditionVo getRegulationStage() {
        List<ConditionDetailsVo> vo = new ArrayList<>();
        Arrays.stream(RuleStage.values()).forEach(e -> {
            ConditionDetailsVo build = ConditionDetailsVo.builder().key(e.getCode()).value(e.getText()).build();
            vo.add(build);
        });
        ConditionVo conditionVo = new ConditionVo(StrUtil.toCamelCase(REGULATION_STAGE), vo);
        return conditionVo;
    }

    /**
     * @return com.voc.service.insights.engine.vo.ConditionVo
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/4/12 14:05
     * @描述 规则条件
     **/
    protected ConditionVo getRegulationRelations() {
        List<ConditionDetailsVo> vo = new ArrayList<>();
        Arrays.stream(RuLerelations.values()).forEach(e -> {
            ConditionDetailsVo build = ConditionDetailsVo.builder().key(e.getCode()).value(e.getText()).build();
            vo.add(build);
        });
        ConditionVo conditionVo = new ConditionVo(StrUtil.toCamelCase(REGULATION_RELATIONS), vo);
        return conditionVo;
    }

    /**
     * @return com.voc.service.insights.engine.vo.ConditionVo
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/4/12 14:04
     * @描述 规则分类
     **/
    protected ConditionVo getRegulationClassify() {
        List<ConditionDetailsVo> vo = new ArrayList<>();
        ConditionDetailsVo general = ConditionDetailsVo.builder().key(RuleClassify.REGULATION_GENERAL.getCode()).value(RuleClassify.REGULATION_GENERAL.getText()).build();
        ConditionDetailsVo custom = ConditionDetailsVo.builder().key(RuleClassify.REGULATION_CUSTOM.getCode()).value(RuleClassify.REGULATION_CUSTOM.getText()).build();
        vo.add(general);
        vo.add(custom);
        ConditionVo conditionVo = new ConditionVo(StrUtil.toCamelCase(REGULATION_CLASSIFY), vo);
        return conditionVo;
    }

    /**
     * @return com.voc.service.insights.engine.vo.ConditionVo
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/4/12 14:04
     * @描述 规则权重
     **/
    protected ConditionVo getRuleWeight() {
        List<ConditionDetailsVo> vo = new ArrayList<>();
        Arrays.stream(RuleWeight.values()).forEach(e -> {
            ConditionDetailsVo build = ConditionDetailsVo.builder().key(e.getCode()).build();
            vo.add(build);
        });
        ConditionVo conditionVo = new ConditionVo(StrUtil.toCamelCase(RULE_WEIGHT), vo);
        return conditionVo;
    }

    /**
     * @return com.voc.service.insights.engine.vo.ConditionVo
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/4/12 14:03
     * @描述 规则条件类型
     **/
    protected ConditionVo getRuleConditionType() {
        List<ConditionDetailsVo> vo = new ArrayList<>();
        Arrays.stream(RuleConditionType.values()).forEach(e -> {
            ConditionDetailsVo build = ConditionDetailsVo.builder().key(e.getCode()).value(e.getText()).build();
            vo.add(build);
        });
        ConditionVo conditionVo = new ConditionVo(StrUtil.toCamelCase(RULE_CONDITION_TYPE), vo);
        return conditionVo;
    }

    /**
     * @return com.voc.service.insights.engine.vo.ConditionVo
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/4/12 14:03
     * @描述 规则逻辑运算符
     **/
    protected ConditionVo getRuleLogicalOperator() {
        final List<ConditionDetailsVo> vo = Arrays.stream(RuleLogicalOperator.values())
                .map(e -> ConditionDetailsVo.builder().key(e.getCode()).value(e.getText()).build())
                .collect(Collectors.toList());

        ConditionVo conditionVo = new ConditionVo(StrUtil.toCamelCase(RULE_LOGICAL_OPERATOR), vo);
        return conditionVo;
    }

    /**
     * @return com.voc.service.insights.engine.vo.ConditionVo
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/4/12 14:10
     * @描述 规则变量值
     **/
    protected ConditionVo getRuleVariableValue() {
        final List<ConditionDetailsVo> vo = Arrays.stream(RuleVariableType.values())
                .map(e -> ConditionDetailsVo.builder().key(e.getCode()).value(e.getText()).build())
                .collect(Collectors.toList());
        ConditionVo conditionVo = new ConditionVo(StrUtil.toCamelCase(VARIABLE_VALUE), vo);
        return conditionVo;
    }


    /**
     * @return com.voc.service.insights.engine.vo.ConditionVo
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/5/21 下午5:32
     * @描述 资源组类型
     **/
    protected ConditionVo getResourceGroupType() {
        List<ConditionDetailsVo> vo = new ArrayList<>();
        final boolean admin = ServiceContextHolder.isAdmin();
        if (admin) {
            //管理员
            vo = Arrays.stream(ResourceGroupType.values()).map(e -> {
                ConditionDetailsVo build = ConditionDetailsVo.builder().key(e.getCode()).value(e.getText()).build();
                return build;
            }).collect(Collectors.toList());
        } else {
            //非管理员
            vo.add(ConditionDetailsVo.builder().key(ResourceGroupType.CUSTOM.getCode()).value(ResourceGroupType.CUSTOM.getText()).build());
        }
        ConditionVo conditionVo = new ConditionVo(StrUtil.toCamelCase(RESOURCE_GROUP_TYPE), vo);
        return conditionVo;
    }


    protected ConditionVo getTagLibAttribute() {
        List<ConditionDetailsVo> collect = Arrays.stream(TagAttribute.values())
                .sorted(Comparator.comparing(TagAttribute::getLevel, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(e -> {
                    ConditionDetailsVo build = ConditionDetailsVo.builder().key(e.getCode()).value(e.getText()).build();
                    return build;
                }).collect(Collectors.toList());
        ConditionVo conditionVo = new ConditionVo(StrUtil.toCamelCase(TAG_LIB_ATTRIBUTE), collect);
        return conditionVo;
    }


    protected ConditionVo getCategoryType() {
        List<ConditionDetailsVo> collect = Arrays.stream(CategoryTypeEnum.values())
                .sorted(Comparator.comparing(CategoryTypeEnum::getSort, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(e -> {
                    ConditionDetailsVo build = ConditionDetailsVo.builder().key(e.getCode()).value(e.getName()).build();
                    return build;
                }).collect(Collectors.toList());
        ConditionVo conditionVo = new ConditionVo(StrUtil.toCamelCase(CATEGORY_TYPE), collect);
        return conditionVo;
    }

    protected ConditionVo getIncreaseType() {
        List<ConditionDetailsVo> collect = Arrays.stream(IncreaseTypeEnum.values())
                .sorted(Comparator.comparing(IncreaseTypeEnum::getCode, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(e -> {
                    ConditionDetailsVo build = ConditionDetailsVo.builder().key(e.getCode()).value(e.getName()).build();
                    return build;
                }).collect(Collectors.toList());
        ConditionVo conditionVo = new ConditionVo(StrUtil.toCamelCase(INCREASE_TYPE), collect);
        return conditionVo;
    }

    protected ConditionVo getEnableStatus() {
        List<ConditionDetailsVo> collect = Arrays.stream(EnableStatusEnum.values())
                .sorted(Comparator.comparing(EnableStatusEnum::getCode, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(e -> {
                    ConditionDetailsVo build = ConditionDetailsVo.builder().key(e.getCode()).value(e.getName()).build();
                    return build;
                }).collect(Collectors.toList());
        ConditionVo conditionVo = new ConditionVo(StrUtil.toCamelCase(ENABLE_STATUS), collect);
        return conditionVo;
    }

    protected ConditionVo getAuditStatus() {
        List<ConditionDetailsVo> collect = Arrays.stream(AuditStatusEnum.values())
                .sorted(Comparator.comparing(AuditStatusEnum::getCode, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(e -> {
                    ConditionDetailsVo build = ConditionDetailsVo.builder().key(e.getCode()).value(e.getName()).build();
                    return build;
                }).collect(Collectors.toList());
        ConditionVo conditionVo = new ConditionVo(StrUtil.toCamelCase(AUDIT_STATUS), collect);
        return conditionVo;
    }

    protected ConditionVo getAccountStatus() {
        List<ConditionDetailsVo> collect = Arrays.stream(AccountStatusEnum.values())
                .sorted(Comparator.comparing(AccountStatusEnum::getCode, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(e -> {
                    ConditionDetailsVo build = ConditionDetailsVo.builder().key(e.getCode()).value(e.getName()).build();
                    return build;
                }).collect(Collectors.toList());
        ConditionVo conditionVo = new ConditionVo(StrUtil.toCamelCase(ACCOUNT_STATUS), collect);
        return conditionVo;
    }

    private ConditionVo getAutoMark() {
        List<AutomarkVo> automarkInfoList = automarkService.findAutomarkInfoList(new InsAutomarkModel());
        List<ConditionDetailsVo> res = new ArrayList<>();
        automarkInfoList.stream().forEach(e -> {
            ConditionDetailsVo build = ConditionDetailsVo.builder().key(e.getId()).value(e.getName()).build();
            res.add(build);
        });
        return new ConditionVo(StrUtil.toCamelCase(AUTOMARK), res);
    }

    /**
     * @return com.voc.service.insights.engine.vo.ConditionVo
     * @描述 获取客户性别
     **/ 
    protected ConditionVo getCustomerGender() {
        List<ConditionDetailsVo> vo = new ArrayList<>();
        vo.add(ConditionDetailsVo.builder().key("M").value("男").build());
        vo.add(ConditionDetailsVo.builder().key("F").value("女").build());
        ConditionVo conditionVo = new ConditionVo(StrUtil.toCamelCase(CUSTOMER_GENDER), vo);
        return conditionVo;
    }

    /**
     * @return com.voc.service.insights.engine.vo.ConditionVo
     * @描述 获取是否水军
     **/ 
    protected ConditionVo getWaterMan() {
        List<ConditionDetailsVo> vo = new ArrayList<>();
        vo.add(ConditionDetailsVo.builder().key("1").value("是").build());
        vo.add(ConditionDetailsVo.builder().key("0").value("否").build());
        ConditionVo conditionVo = new ConditionVo(StrUtil.toCamelCase(WATER_MAN), vo);
        return conditionVo;
    }

    /**
     * @return com.voc.service.insights.engine.vo.ConditionVo
     * @描述 获取是否大V
     **/ 
    protected ConditionVo getVMan() {
        List<ConditionDetailsVo> vo = new ArrayList<>();
        vo.add(ConditionDetailsVo.builder().key("1").value("是").build());
        vo.add(ConditionDetailsVo.builder().key("0").value("否").build());
        ConditionVo conditionVo = new ConditionVo(StrUtil.toCamelCase(V_MAN), vo);
        return conditionVo;
    }

    /**
     * @return com.voc.service.insights.engine.vo.ConditionVo
     * @描述 获取是否车主
     **/ 
    protected ConditionVo getCarOwner() {
        List<ConditionDetailsVo> vo = new ArrayList<>();
        vo.add(ConditionDetailsVo.builder().key("1").value("是").build());
        vo.add(ConditionDetailsVo.builder().key("0").value("否").build());
        ConditionVo conditionVo = new ConditionVo(StrUtil.toCamelCase(CAR_OWNER), vo);
        return conditionVo;
    }


    @GetMapping("/getChannelTree")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询渠道树")
    public Result<?> getChannelTree(@RequestParam(required = false) String clientId, @RequestParam(required = false) Integer level) {
        List<ChannelInfoVo> channelTree = channelInfoService.findChannelTree(InsChannelInfoModel.builder().clientId(clientId).level(level).build());
        return Result.OK(channelTree);
    }


    @GetMapping("/findTagLibCategoryTree")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询标签分类树")
    public Result<?> findTagLibCategoryTree(@RequestParam(required = false) String clientId, @RequestParam(required = false) String tagLibType) {
        return Result.OK(iInsTagLibService.findTagLibCategoryTree(clientId, tagLibType));
    }


    public abstract Object conditions();

    protected Set<ConditionVo> async(Set<String> list) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("condition async 开始");
        List<CompletableFuture<ConditionVo>> futureList = new CopyOnWriteArrayList<>();
        Set<ConditionVo> rs = Collections.synchronizedSet(new HashSet<>());
        stopWatch.stop();
        stopWatch.start("初始化");
        list.stream().forEach(key -> {
//            futureList.add(CompletableFuture.supplyAsync(SupplierWrapper.of(() -> get(key))));
            futureList.add(CompletableFuture.supplyAsync(TtlWrappers.wrapSupplier(() -> {
                stopWatch.start("async get".concat(key));
                ConditionVo vo = get(key);
                stopWatch.stop();
                return vo;
            })));
        });
        stopWatch.stop();
        stopWatch.start("执行异步任务集合");
        try {
            futureList.stream().forEach(f -> {
                try {
                    rs.add(f.get(1000 * 5, TimeUnit.MILLISECONDS));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            log.error("[获取过滤条件数据异常]", e.getMessage(), e);
        } finally {
            stopWatch.stop();
            stopWatch.prettyPrint();
        }

        return rs;
    }
}
