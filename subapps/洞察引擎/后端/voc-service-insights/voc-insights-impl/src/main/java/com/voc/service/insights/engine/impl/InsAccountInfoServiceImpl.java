package com.voc.service.insights.engine.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.PatternPool;
import cn.hutool.core.util.ReUtil;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.model.UserModel;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.common.util.StopWatch;
import com.voc.service.insights.engine.api.*;
import com.voc.service.insights.engine.common.enums.InsCommonErrorEnum;
import com.voc.service.insights.engine.dao.InsAccountInfoDao;
import com.voc.service.insights.engine.mapper.InsAccountTreeUserMapper;
import com.voc.service.insights.engine.model.*;
import com.voc.service.insights.engine.vo.*;
import com.voc.service.security.model.ChangePasswordRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @创建者: fanrong
 * @创建时间: 2024/3/4 13:52
 * @描述:
 **/
@Service
public class InsAccountInfoServiceImpl implements IInsAccountInfoService {
    private static final Logger log = LoggerFactory.getLogger(InsAccountInfoServiceImpl.class);
    private static final String ROOT_PARENT_ID = "-1";
    private static final int MAX_DEPART_DEPTH = 3;
    private static final String DEPART_ACCOUNT_TREE_CACHE_KEY = "departAccountTree";
    private static final String DEPART_ACCOUNT_TREE_CACHE_KEY_PREFIX = DEPART_ACCOUNT_TREE_CACHE_KEY + ":";
    private static final String DEPART_ACCOUNT_SUBTREE_CACHE_KEY_PREFIX = "departAccountSubTree:v4:";

    @Autowired
    InsAccountInfoDao accountInfoDao;
    @Autowired
    IInsUserRoleService iInsUserRoleService;

    @Autowired
    IInsRoleService iInsRoleService;

    @Autowired
    IInsStaSysDepartService iInsReportStaSysDepartService;
    @Autowired
    IInsStaSysUserDepartService iInsStaSysUserDepartService;

    private final String clientId = "764547797eb2e192763f5334028d49c9";

    @Autowired
    InsAccountTreeUserMapper accountTreeUserMapper;

    @CreateCache(area = "VDP", name = ":insights:departAccount", expire = 6, timeUnit = TimeUnit.HOURS, cacheType = CacheType.REMOTE)
    private Cache<String,List<InsSysDepartVo>> departAccountTreeCache;



    @Override
    public void saveAccountInfo(InsAccountInfoModel accountInfoModel) {
        //参数校验
        this.checkParameter(accountInfoModel);
        Assert.hasLength(accountInfoModel.getAccountPwd(), "账号密码不允许为空");
        final String appId = ServiceContextHolder.getSystemId();
        //用户数据组装
        UserModel userModel = this.userDataAssembly(accountInfoModel);
        //注册账号
        accountInfoDao.registerAccountInfo(userModel);
        userModel.setAppId(appId);
        List<UserModel> accountInfo = accountInfoDao.findAccountInfoList(userModel);
        UserModel userInfo = accountInfo.stream().findFirst().orElse(null);
        if (ObjectUtils.isEmpty(userInfo)) {
            throw new BussinessException(InsCommonErrorEnum.REGISTER_ACCOUNT_ERROR);
        }
        saveOrUpdateRole(accountInfoModel.getRoleId(), userInfo.getUserId(), accountInfoModel.getClientId());
    }

    @Override
    public void updateAccountInfo(InsAccountInfoModel accountInfoModel) {
        //参数校验
        Assert.hasLength(accountInfoModel.getUserId(), "userId不允许为空");
        Assert.hasLength(accountInfoModel.getRoleId(), "角色不允许为空");
        Assert.hasLength(accountInfoModel.getStatus(), "状态不允许为空");
        final String appId = ServiceContextHolder.getSystemId();
        if (ObjectUtils.isNotEmpty(accountInfoModel.getAccountPwd())) {
            //重置密码
            ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.builder()
                    .type("base")
                    .appId(appId)
                    .userId(accountInfoModel.getUserId())
                    .newPassword(accountInfoModel.getAccountPwd())
                    .confirmationPassword(accountInfoModel.getAccountPwd())
                    .build();
            accountInfoDao.resetPassword(changePasswordRequest);
        }
        //用户数据组装
        UserModel userModel = this.userDataAssembly(accountInfoModel);
        userModel.setUserId(accountInfoModel.getUserId());
        accountInfoDao.modifyAccountInfo(userModel);
        if(ObjectUtils.isEmpty(accountInfoModel.getRoleId())){
            deleteRole(accountInfoModel.getUserId(), accountInfoModel.getClientId());
        }else{
            saveOrUpdateRole(accountInfoModel.getRoleId(), accountInfoModel.getUserId(), accountInfoModel.getClientId());
        }
    }

    private void saveOrUpdateRole(String roleId, String userId, String clientId) {
        try {
            InsUserRoleModel insUserRoleModel = new InsUserRoleModel();
            insUserRoleModel.setRoleId(roleId);
            insUserRoleModel.setUserId(userId);
            insUserRoleModel.setClientId(clientId);
            iInsUserRoleService.saveOrUpdate(insUserRoleModel);
        } catch (Exception e) {
            throw new BussinessException(InsCommonErrorEnum.REGISTER_ROLE_ERROR);
        }
    }

    private void deleteRole(String userId, String clientId) {
        try {
            InsUserRoleModel build = InsUserRoleModel.builder().userId(userId).clientId(clientId).build();
            iInsUserRoleService.deleteRole(build);
        } catch (Exception e) {
            throw new BussinessException(InsCommonErrorEnum.REGISTER_ROLE_ERROR);
        }
    }

    @Override
    public InsAccountInfoVo findAccountInfo(InsAccountInfoModel accountInfoModel) {
        //单独参数校验
        Assert.hasLength(accountInfoModel.getUserId(), "userId不允许为空");
        final String userId = accountInfoModel.getUserId();
        UserModel userModel = UserModel.builder().userId(userId).appId(ServiceContextHolder.getSystemId()).build();
        UserModel accountInfo = accountInfoDao.findAccountInfo(userModel);
        InsUserRoleModel insUserRoleModel = new InsUserRoleModel();
        insUserRoleModel.setUserIdList(Collections.singletonList(userId));
        insUserRoleModel.setClientId(accountInfoModel.getClientId());
        List<InsUserRoleVo> roleInfoList = new ArrayList<>();
        try {
            roleInfoList = iInsUserRoleService.getRoleInfo(insUserRoleModel);
        } catch (Exception e) {
            log.error("获取角色信息异常:", e);
        }
        Map<String, InsUserRoleVo> insUserRoleVoMap = roleInfoList.stream().collect(Collectors.toMap(InsUserRoleVo::getUserId, Function.identity()));
        //账号数据重组
        InsAccountInfoVo accountInfoVo = InsAccountInfoVo.builder()
                .userId(accountInfo.getUserId())
                .accountName(accountInfo.getUsername())
                .userName(accountInfo.getFirstname())
                .deptName("")
                .roleId(insUserRoleVoMap.containsKey(accountInfo.getUserId()) ? insUserRoleVoMap.get(accountInfo.getUserId()).getRoleId() : "")
                .contact(ObjectUtils.isNotEmpty(accountInfo.getPhone()) ? accountInfo.getPhone() : null)
                .position(ObjectUtils.isNotEmpty(accountInfo.getPosition()) ? accountInfo.getPosition() : null)
                .email(ObjectUtils.isNotEmpty(accountInfo.getEmail()) ? accountInfo.getEmail() : null)
                .remark(ObjectUtils.isNotEmpty(accountInfo.getRemark()) ? accountInfo.getRemark() : null)
                .status(accountInfo.isEnabled() ? "1" : "0")
                .roleName(insUserRoleVoMap.containsKey(accountInfo.getUserId()) ? insUserRoleVoMap.get(accountInfo.getUserId()).getRoleName() : "")
                .homePhone(accountInfo.getHomePhone())
                .officePhone(accountInfo.getOfficePhone())
                .phone(accountInfo.getPhone()).build();
        return accountInfoVo;
    }

    @Override
    public PageInfo findAccountInfoList(InsAccountInfoModel accountInfoModel) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("findAccountInfoList.service 开始");

        UserModel userModel = new UserModel();
        if (ObjectUtils.isNotEmpty(accountInfoModel.getClientId())) {
            userModel.setClientIds(Collections.singletonList(accountInfoModel.getClientId()));
        }
        if (ObjectUtils.isNotEmpty(accountInfoModel.getUserName())) {
            userModel.setUsername(accountInfoModel.getUserName());
        }
        if (ObjectUtils.isNotEmpty(accountInfoModel.getStatus())) {
            userModel.setStatus(accountInfoModel.getStatus());
        }
        if(ObjectUtils.isNotEmpty(accountInfoModel.getCompleteRate())){
            userModel.setLoginCompleteRate(accountInfoModel.getCompleteRate());
        }
        if (ObjectUtils.isNotEmpty(accountInfoModel.getDeptId())) {
            List<String> userIdList = iInsStaSysUserDepartService.getDepIdByUserIdList(Arrays.asList(accountInfoModel.getDeptId()),accountInfoModel.getClientId() );
            if (ObjectUtils.isNotEmpty(userIdList)) {
                userModel.setUserIds(userIdList);
            }else {
                userModel.setUserIds(Collections.singletonList("0"));
            }
        }
        userModel.setPageNum(accountInfoModel.getPageNum());
        userModel.setPageSize(accountInfoModel.getPageSize());
        final String appId = ServiceContextHolder.getSystemId();
        userModel.setAppId(appId);
        stopWatch.stop();
        stopWatch.start("findAccountInfoList.service调用dao");
        // 调用dao层方法，传入userModel对象，返回PageInfo对象
        PageInfo pageInfo = accountInfoDao.findAccountInfoByConditional(userModel, accountInfoModel.getClientId(),accountInfoModel.getRoleId());
        stopWatch.stop();
        stopWatch.prettyPrint();
        return pageInfo;
    }

    @Override
    public void deleteAccountInfo(InsAccountInfoModel accountInfoModel) {
        //单独参数校验
        Assert.hasLength(accountInfoModel.getUserId(), "userId不允许为空");
        final String userId = accountInfoModel.getUserId();
        UserModel userModel = UserModel.builder().userId(userId).build();
        accountInfoDao.deleteAccountInfo(userModel);
    }

    @Override
    public List<UserModel> findAllAccountInfoList(UserModel userModel) {
        return accountInfoDao.findAccountInfoList(userModel);
    }

    @Override
    public Result<?> queryRoleALlList(InsRoleQueryModel model) {

        return iInsRoleService.queryRoleALlList(model);
    }

    @Override
    public  List<InsSysDepartModel> findDepartList(InsAccountInfoModel accountInfoModel) {
        return iInsReportStaSysDepartService.getClientDepartList(accountInfoModel);
    }

    @Override
    public List<InsSysDepartVo> findDepartTree(InsAccountInfoModel accountInfoModel) {
        //全部部门
        final List<InsSysDepartVo> departList = iInsReportStaSysDepartService.getDepartList(clientId);
        DepartTreeData departTreeData = buildDepartTreeData(departList);
        this.departUserTree(departTreeData.getTopDepartList(), departTreeData.getChildDepartMap(), Collections.emptyMap(), 1, false, Collections.emptyMap());
        return departTreeData.getTopDepartList();
    }

    @Override
    public List<InsAccountInfoVo> findAccountByDeptId(InsAccountInfoModel accountInfoModel) {
        Assert.hasLength(accountInfoModel.getDeptId(), "部门id不允许为空");
        List<UserModel> accountInfo = findAccountInfoByDeptId(accountInfoModel, resolveClientId(accountInfoModel));
        if (CollUtil.isEmpty(accountInfo)) {
            return List.of();
        }
        return accountInfo.stream().map(e->{
            return InsAccountInfoVo.builder()
                    .userId(e.getUserId())
                    .accountName(e.getUsername())
                    .userName(e.getFirstname())
                    .employeeId(e.getEmployeeId())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public List<InsSysDepartVo> findDepartAccountTree() {
        List<InsSysDepartVo> departAccountTree = getCachedDepartAccountTree(clientId);
        if (departAccountTree != null) {
            log.info("从缓存中获取部门用户树");
            return departAccountTree;
        }

        DepartTreeData departTreeData = buildDepartTreeData(iInsReportStaSysDepartService.getDepartList(clientId));
        Map<String, List<InsAccountVo>> departAccountMap = buildDirectDepartAccountMap();
        Map<String, List<InsAccountVo>> departSubtreeAccountMap = buildDepartSubtreeAccountMap(
                departTreeData.getTopDepartList(),
                departTreeData.getChildDepartMap(),
                departAccountMap
        );
        departAccountTree = departTreeData.getTopDepartList();
        this.departUserTree(departAccountTree, departTreeData.getChildDepartMap(), departAccountMap, 1, true, departSubtreeAccountMap);
        log.info("部门用户树缓存");
        cacheDepartAccountTree(clientId, departAccountTree);
        return departAccountTree;
    }

    @Override
    public List<InsSysDepartVo> findDepartAccountTreeByDeptId(InsAccountInfoModel accountInfoModel) {
        Assert.notNull(accountInfoModel, "请求参数不允许为空");
        Assert.hasLength(accountInfoModel.getDeptId(), "部门id不允许为空");

        String currentClientId = resolveClientId(accountInfoModel);
        List<InsSysDepartVo> cachedDepartSubtree = getCachedDepartAccountSubtree(currentClientId, accountInfoModel.getDeptId());
        if (CollUtil.isNotEmpty(cachedDepartSubtree)) {
            return cachedDepartSubtree;
        }

        List<InsSysDepartVo> ancestorDepartList = iInsReportStaSysDepartService.getDepartAncestorList(currentClientId, accountInfoModel.getDeptId());
        if (CollUtil.isEmpty(ancestorDepartList)) {
            return List.of();
        }

        List<InsSysDepartVo> subtreeDepartList = iInsReportStaSysDepartService.getDepartSubtree(currentClientId, accountInfoModel.getDeptId());
        if (CollUtil.isEmpty(subtreeDepartList)) {
            subtreeDepartList = ancestorDepartList.stream()
                    .filter(depart -> depart != null && Objects.equals(accountInfoModel.getDeptId(), depart.getId()))
                    .collect(Collectors.toList());
        }
        List<InsSysDepartVo> departAccountTree = buildLimitedDepartAccountTree(
                accountInfoModel.getDeptId(),
                ancestorDepartList,
                subtreeDepartList,
                currentClientId
        );
        if (CollUtil.isEmpty(departAccountTree)) {
            return List.of();
        }

        cacheDepartAccountSubtree(currentClientId, accountInfoModel.getDeptId(), departAccountTree);
        return departAccountTree;
    }


    void departUserTree(List<InsSysDepartVo> top, Map<String, List<InsSysDepartVo>> childDepartMap, Map<String, List<InsAccountVo>> departAccountMap, int currentDepth, boolean needUser, Map<String, List<InsAccountVo>> departSubtreeAccountMap) {
        if (ObjectUtils.isEmpty(top)) {
            return;
        }
        if (currentDepth > MAX_DEPART_DEPTH) {
            return;
        }

        for (InsSysDepartVo depart : top) {
            List<InsSysDepartVo> tagLibCategoryVos = childDepartMap.get(depart.getId());
            int nextDepth = currentDepth + 1;
            this.departUserTree(tagLibCategoryVos, childDepartMap, departAccountMap, nextDepth, needUser, departSubtreeAccountMap);
            if (nextDepth <= MAX_DEPART_DEPTH) {
                depart.setChild(tagLibCategoryVos);
            }

            if (!needUser) {
                continue;
            }

            List<InsAccountVo> accountList;
            if (nextDepth > MAX_DEPART_DEPTH) {
                accountList = departSubtreeAccountMap.get(depart.getId());
            } else {
                accountList = departAccountMap.get(depart.getCode());
            }
            if (ObjectUtils.isNotEmpty(accountList)) {
                depart.setAccount(accountList);
            }
        }
    }

    private Map<String, List<InsAccountVo>> buildDirectDepartAccountMap() {
        return buildDirectDepartAccountMap(clientId);
    }

    private Map<String, List<InsAccountVo>> buildDirectDepartAccountMap(String currentClientId) {
        return buildDirectDepartAccountMap(currentClientId, Collections.emptySet());
    }

    private Map<String, List<InsAccountVo>> buildDirectDepartAccountMap(String currentClientId, Collection<String> deptCodes) {
        UserModel userQuery = UserModel.builder()
                .clientId(currentClientId)
                .appId(ServiceContextHolder.getSystemId())
                .build();
        List<InsDepartAccountRelationVo> departAccountRelationList = accountTreeUserMapper.findDepartAccountRelationList(
                userQuery,
                CollUtil.isEmpty(deptCodes) ? null : deptCodes
        );
        if (CollUtil.isEmpty(departAccountRelationList)) {
            return Collections.emptyMap();
        }
        Map<String, LinkedHashMap<String, InsAccountVo>> directAccountMap = new HashMap<>();
        for (InsDepartAccountRelationVo relation : departAccountRelationList) {
            if (relation == null || ObjectUtils.isEmpty(relation.getDeptCode()) || ObjectUtils.isEmpty(relation.getUserId())) {
                continue;
            }
            directAccountMap
                    .computeIfAbsent(relation.getDeptCode(), key -> new LinkedHashMap<>())
                    .putIfAbsent(relation.getUserId(), buildAccount(relation));
        }

        Map<String, List<InsAccountVo>> departAccountMap = new HashMap<>(directAccountMap.size());
        directAccountMap.forEach((deptCode, accountMap) -> departAccountMap.put(deptCode, new ArrayList<>(accountMap.values())));
        return departAccountMap;
    }

    private List<UserModel> findAccountInfoByDeptId(InsAccountInfoModel accountInfoModel, String currentClientId) {
        if (accountInfoModel == null || ObjectUtils.isEmpty(accountInfoModel.getDeptId())) {
            return List.of();
        }

        List<String> userIdList = iInsStaSysUserDepartService.findUserIdByDepId(accountInfoModel.getDeptId(), currentClientId);
        if (CollUtil.isEmpty(userIdList)) {
            return List.of();
        }

        UserModel userModel = new UserModel();
        if (ObjectUtils.isNotEmpty(currentClientId)) {
            userModel.setClientIds(Collections.singletonList(currentClientId));
        }
        userModel.setUserIds(new ArrayList<>(new LinkedHashSet<>(userIdList)));
        userModel.setAppId(ServiceContextHolder.getSystemId());
        userModel.setStatus("1");
        return accountInfoDao.findAccountInfoList(userModel);
    }

    private Map<String, List<InsAccountVo>> buildScopedDepartAccountMap(List<UserModel> accountInfoList, String currentClientId, Set<String> subtreeDeptCodeSet) {
        if (CollUtil.isEmpty(accountInfoList) || CollUtil.isEmpty(subtreeDeptCodeSet)) {
            return Collections.emptyMap();
        }

        Map<String, InsAccountVo> accountMap = accountInfoList.stream()
                .filter(Objects::nonNull)
                .filter(account -> ObjectUtils.isNotEmpty(account.getUserId()))
                .collect(Collectors.toMap(
                        UserModel::getUserId,
                        this::buildAccount,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
        if (accountMap.isEmpty()) {
            return Collections.emptyMap();
        }

        InsStaSysUserDepartModel relationQuery = InsStaSysUserDepartModel.builder()
                .userIds(new ArrayList<>(accountMap.keySet()))
                .build();
        List<InsStaSysUserDepartVo> departRelationList = iInsStaSysUserDepartService.findStaSysUserDepartList(relationQuery, currentClientId);
        if (CollUtil.isEmpty(departRelationList)) {
            return Collections.emptyMap();
        }

        Map<String, LinkedHashMap<String, InsAccountVo>> directAccountMap = new HashMap<>();
        for (InsStaSysUserDepartVo relation : departRelationList) {
            if (relation == null || ObjectUtils.isEmpty(relation.getDepId()) || ObjectUtils.isEmpty(relation.getUserId())) {
                continue;
            }
            if (!subtreeDeptCodeSet.contains(relation.getDepId())) {
                continue;
            }
            InsAccountVo account = accountMap.get(relation.getUserId());
            if (account == null) {
                continue;
            }
            directAccountMap
                    .computeIfAbsent(relation.getDepId(), key -> new LinkedHashMap<>())
                    .putIfAbsent(relation.getUserId(), copyAccount(account));
        }

        Map<String, List<InsAccountVo>> departAccountMap = new HashMap<>(directAccountMap.size());
        directAccountMap.forEach((deptCode, accountByDeptMap) -> departAccountMap.put(deptCode, new ArrayList<>(accountByDeptMap.values())));
        return departAccountMap;
    }

    private Map<String, List<InsAccountVo>> buildDepartSubtreeAccountMap(List<InsSysDepartVo> topDepartList, Map<String, List<InsSysDepartVo>> childDepartMap, Map<String, List<InsAccountVo>> departAccountMap) {
        if (CollUtil.isEmpty(topDepartList)) {
            return Collections.emptyMap();
        }
        Map<String, List<InsAccountVo>> departSubtreeAccountMap = new HashMap<>(childDepartMap.size());
        for (InsSysDepartVo depart : topDepartList) {
            collectDepartSubtreeAccounts(depart, childDepartMap, departAccountMap, departSubtreeAccountMap);
        }
        return departSubtreeAccountMap;
    }

    private List<InsAccountVo> collectDepartSubtreeAccounts(InsSysDepartVo depart, Map<String, List<InsSysDepartVo>> childDepartMap, Map<String, List<InsAccountVo>> departAccountMap, Map<String, List<InsAccountVo>> departSubtreeAccountMap) {
        if (depart == null || ObjectUtils.isEmpty(depart.getId())) {
            return Collections.emptyList();
        }
        List<InsAccountVo> cachedAccountList = departSubtreeAccountMap.get(depart.getId());
        if (cachedAccountList != null) {
            return cachedAccountList;
        }

        LinkedHashMap<String, InsAccountVo> mergedAccountMap = new LinkedHashMap<>();
        mergeAccountList(mergedAccountMap, departAccountMap.get(depart.getCode()));
        List<InsSysDepartVo> childDepartList = childDepartMap.get(depart.getId());
        if (CollUtil.isNotEmpty(childDepartList)) {
            for (InsSysDepartVo childDepart : childDepartList) {
                mergeAccountList(mergedAccountMap, collectDepartSubtreeAccounts(childDepart, childDepartMap, departAccountMap, departSubtreeAccountMap));
            }
        }

        List<InsAccountVo> accountList = mergedAccountMap.isEmpty() ? Collections.emptyList() : new ArrayList<>(mergedAccountMap.values());
        departSubtreeAccountMap.put(depart.getId(), accountList);
        return accountList;
    }

    private void mergeAccountList(Map<String, InsAccountVo> mergedAccountMap, List<InsAccountVo> accountList) {
        if (mergedAccountMap == null || CollUtil.isEmpty(accountList)) {
            return;
        }
        for (InsAccountVo account : accountList) {
            if (account == null || ObjectUtils.isEmpty(account.getId())) {
                continue;
            }
            mergedAccountMap.putIfAbsent(account.getId(), account);
        }
    }

    private InsAccountVo buildAccount(InsDepartAccountRelationVo relation) {
        return InsAccountVo.builder()
                .id(relation.getUserId())
                .name(relation.getUserName())
                .employeeId(ObjectUtils.isEmpty(relation.getEmployeeId()) ? "" : relation.getEmployeeId())
                .build();
    }

    private InsAccountVo buildAccount(UserModel userModel) {
        return InsAccountVo.builder()
                .id(userModel.getUserId())
                .name(userModel.getFirstname())
                .employeeId(ObjectUtils.isEmpty(userModel.getEmployeeId()) ? "" : userModel.getEmployeeId())
                .build();
    }

    private InsSysDepartVo copyDepartNode(InsSysDepartVo depart) {
        if (depart == null) {
            return null;
        }
        return InsSysDepartVo.builder()
                .id(depart.getId())
                .name(depart.getName())
                .code(depart.getCode())
                .parentId(depart.getParentId())
                .build();
    }

    private InsAccountVo copyAccount(InsAccountVo account) {
        if (account == null) {
            return null;
        }
        return InsAccountVo.builder()
                .id(account.getId())
                .name(account.getName())
                .employeeId(account.getEmployeeId())
                .build();
    }

    private List<InsAccountVo> copyAccountList(List<InsAccountVo> accountList) {
        if (CollUtil.isEmpty(accountList)) {
            return Collections.emptyList();
        }
        return accountList.stream()
                .map(this::copyAccount)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<InsSysDepartVo> buildLimitedDepartAccountTree(String currentDeptId,
                                                               List<InsSysDepartVo> ancestorDepartList,
                                                               List<InsSysDepartVo> subtreeDepartList,
                                                               String currentClientId) {
        List<InsSysDepartVo> mergedDepartList = mergeDepartList(ancestorDepartList, subtreeDepartList);
        if (CollUtil.isEmpty(mergedDepartList)) {
            return Collections.emptyList();
        }

        Map<String, InsSysDepartVo> departMap = buildDepartMapById(mergedDepartList);
        List<InsSysDepartVo> ancestorChain = buildAncestorChain(currentDeptId, departMap);
        if (CollUtil.isEmpty(ancestorChain)) {
            return Collections.emptyList();
        }

        List<InsSysDepartVo> visibleChain = new ArrayList<>(ancestorChain.subList(0, Math.min(MAX_DEPART_DEPTH, ancestorChain.size())));
        LinkedHashMap<String, InsSysDepartVo> visibleDepartMap = buildVisibleDepartMap(visibleChain);
        if (visibleDepartMap.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> subtreeDeptCodeSet = subtreeDepartList.stream()
                .map(InsSysDepartVo::getCode)
                .filter(ObjectUtils::isNotEmpty)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<String, List<InsAccountVo>> directDepartAccountMap = buildDirectDepartAccountMap(currentClientId, subtreeDeptCodeSet);
        Map<String, String> visibleDeptTargetMap = buildVisibleDeptTargetMap(subtreeDepartList, departMap, visibleDepartMap.keySet());
        attachVisibleAccounts(subtreeDepartList, directDepartAccountMap, visibleDeptTargetMap, visibleDepartMap);

        return Collections.singletonList(visibleDepartMap.values().iterator().next());
    }

    private Map<String, InsSysDepartVo> buildDepartMapById(List<InsSysDepartVo> departList) {
        if (CollUtil.isEmpty(departList)) {
            return Collections.emptyMap();
        }
        Map<String, InsSysDepartVo> departMap = new LinkedHashMap<>(departList.size());
        for (InsSysDepartVo depart : departList) {
            if (depart == null || ObjectUtils.isEmpty(depart.getId())) {
                continue;
            }
            departMap.putIfAbsent(depart.getId(), depart);
        }
        return departMap;
    }

    private List<InsSysDepartVo> buildAncestorChain(String deptId, Map<String, InsSysDepartVo> departMap) {
        if (ObjectUtils.isEmpty(deptId) || departMap == null || departMap.isEmpty()) {
            return Collections.emptyList();
        }

        LinkedList<InsSysDepartVo> ancestorChain = new LinkedList<>();
        String currentDeptId = deptId;
        Set<String> visitedDeptIdSet = new HashSet<>();
        while (ObjectUtils.isNotEmpty(currentDeptId) && visitedDeptIdSet.add(currentDeptId)) {
            InsSysDepartVo currentDepart = departMap.get(currentDeptId);
            if (currentDepart == null) {
                break;
            }
            ancestorChain.addFirst(currentDepart);
            if (ObjectUtils.isEmpty(currentDepart.getParentId()) || ROOT_PARENT_ID.equals(currentDepart.getParentId())) {
                break;
            }
            currentDeptId = currentDepart.getParentId();
        }
        return ancestorChain;
    }

    private LinkedHashMap<String, InsSysDepartVo> buildVisibleDepartMap(List<InsSysDepartVo> visibleChain) {
        if (CollUtil.isEmpty(visibleChain)) {
            return new LinkedHashMap<>();
        }

        LinkedHashMap<String, InsSysDepartVo> visibleDepartMap = new LinkedHashMap<>(visibleChain.size());
        InsSysDepartVo previousDepart = null;
        for (InsSysDepartVo depart : visibleChain) {
            if (depart == null || ObjectUtils.isEmpty(depart.getId())) {
                continue;
            }
            InsSysDepartVo currentDepart = copyDepartNode(depart);
            visibleDepartMap.put(currentDepart.getId(), currentDepart);
            if (previousDepart != null) {
                previousDepart.setChild(Collections.singletonList(currentDepart));
            }
            previousDepart = currentDepart;
        }
        return visibleDepartMap;
    }

    private Map<String, String> buildVisibleDeptTargetMap(List<InsSysDepartVo> subtreeDepartList,
                                                          Map<String, InsSysDepartVo> departMap,
                                                          Set<String> visibleDeptIdSet) {
        if (CollUtil.isEmpty(subtreeDepartList) || departMap == null || departMap.isEmpty() || CollUtil.isEmpty(visibleDeptIdSet)) {
            return Collections.emptyMap();
        }

        Map<String, String> visibleDeptTargetMap = new HashMap<>(subtreeDepartList.size());
        for (InsSysDepartVo depart : subtreeDepartList) {
            if (depart == null || ObjectUtils.isEmpty(depart.getId())) {
                continue;
            }
            String visibleDeptId = findNearestVisibleDeptId(depart.getId(), departMap, visibleDeptIdSet);
            if (ObjectUtils.isNotEmpty(visibleDeptId)) {
                visibleDeptTargetMap.put(depart.getId(), visibleDeptId);
            }
        }
        return visibleDeptTargetMap;
    }

    private String findNearestVisibleDeptId(String deptId, Map<String, InsSysDepartVo> departMap, Set<String> visibleDeptIdSet) {
        if (ObjectUtils.isEmpty(deptId) || departMap == null || departMap.isEmpty() || CollUtil.isEmpty(visibleDeptIdSet)) {
            return null;
        }

        String currentDeptId = deptId;
        Set<String> visitedDeptIdSet = new HashSet<>();
        while (ObjectUtils.isNotEmpty(currentDeptId) && visitedDeptIdSet.add(currentDeptId)) {
            if (visibleDeptIdSet.contains(currentDeptId)) {
                return currentDeptId;
            }
            InsSysDepartVo currentDepart = departMap.get(currentDeptId);
            if (currentDepart == null || ObjectUtils.isEmpty(currentDepart.getParentId())) {
                break;
            }
            currentDeptId = currentDepart.getParentId();
        }
        return null;
    }

    private void attachVisibleAccounts(List<InsSysDepartVo> subtreeDepartList,
                                       Map<String, List<InsAccountVo>> directDepartAccountMap,
                                       Map<String, String> visibleDeptTargetMap,
                                       Map<String, InsSysDepartVo> visibleDepartMap) {
        if (CollUtil.isEmpty(subtreeDepartList) || visibleDeptTargetMap == null || visibleDeptTargetMap.isEmpty() || visibleDepartMap == null || visibleDepartMap.isEmpty()) {
            return;
        }

        Map<String, LinkedHashMap<String, InsAccountVo>> visibleAccountMap = new HashMap<>(visibleDepartMap.size());
        for (InsSysDepartVo depart : subtreeDepartList) {
            if (depart == null || ObjectUtils.isEmpty(depart.getId()) || ObjectUtils.isEmpty(depart.getCode())) {
                continue;
            }

            String visibleDeptId = visibleDeptTargetMap.get(depart.getId());
            List<InsAccountVo> accountList = directDepartAccountMap.get(depart.getCode());
            if (ObjectUtils.isEmpty(visibleDeptId) || CollUtil.isEmpty(accountList)) {
                continue;
            }

            LinkedHashMap<String, InsAccountVo> mergedAccountMap = visibleAccountMap.computeIfAbsent(visibleDeptId, key -> new LinkedHashMap<>());
            for (InsAccountVo account : accountList) {
                if (account == null || ObjectUtils.isEmpty(account.getId())) {
                    continue;
                }
                mergedAccountMap.putIfAbsent(account.getId(), copyAccount(account));
            }
        }

        visibleAccountMap.forEach((deptId, accountMap) -> {
            InsSysDepartVo depart = visibleDepartMap.get(deptId);
            if (depart != null && accountMap != null && !accountMap.isEmpty()) {
                depart.setAccount(new ArrayList<>(accountMap.values()));
            }
        });
    }

    private DepartTreeData buildDepartTreeData(List<InsSysDepartVo> departList) {
        List<InsSysDepartVo> topDepartList = new ArrayList<>();
        Map<String, List<InsSysDepartVo>> childDepartMap = new HashMap<>();
        if (CollUtil.isEmpty(departList)) {
            return new DepartTreeData(topDepartList, childDepartMap);
        }

        for (InsSysDepartVo depart : departList) {
            if (depart == null || ObjectUtils.isEmpty(depart.getParentId())) {
                continue;
            }
            if (ROOT_PARENT_ID.equals(depart.getParentId())) {
                topDepartList.add(depart);
                continue;
            }
            childDepartMap.computeIfAbsent(depart.getParentId(), key -> new ArrayList<>()).add(depart);
        }
        return new DepartTreeData(topDepartList, childDepartMap);
    }

    private DepartTreeData buildScopedDepartTreeData(String rootDeptId, List<InsSysDepartVo> departList) {
        List<InsSysDepartVo> topDepartList = new ArrayList<>(1);
        Map<String, List<InsSysDepartVo>> childDepartMap = new HashMap<>();
        if (CollUtil.isEmpty(departList) || ObjectUtils.isEmpty(rootDeptId)) {
            return new DepartTreeData(topDepartList, childDepartMap);
        }

        for (InsSysDepartVo depart : departList) {
            if (depart == null || ObjectUtils.isEmpty(depart.getId())) {
                continue;
            }
            if (rootDeptId.equals(depart.getId())) {
                topDepartList.add(depart);
                continue;
            }
            if (ObjectUtils.isEmpty(depart.getParentId())) {
                continue;
            }
            childDepartMap.computeIfAbsent(depart.getParentId(), key -> new ArrayList<>()).add(depart);
        }
        return new DepartTreeData(topDepartList, childDepartMap);
    }

    private List<InsSysDepartVo> mergeDepartList(List<InsSysDepartVo> ancestorDepartList, List<InsSysDepartVo> subtreeDepartList) {
        LinkedHashMap<String, InsSysDepartVo> departMap = new LinkedHashMap<>();
        mergeDepartList(departMap, ancestorDepartList);
        mergeDepartList(departMap, subtreeDepartList);
        if (departMap.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(departMap.values());
    }

    private void mergeDepartList(Map<String, InsSysDepartVo> departMap, List<InsSysDepartVo> departList) {
        if (departMap == null || CollUtil.isEmpty(departList)) {
            return;
        }
        for (InsSysDepartVo depart : departList) {
            if (depart == null || ObjectUtils.isEmpty(depart.getId())) {
                continue;
            }
            departMap.putIfAbsent(depart.getId(), depart);
        }
    }

    private String resolveClientId(InsAccountInfoModel accountInfoModel) {
        if (accountInfoModel != null && ObjectUtils.isNotEmpty(accountInfoModel.getClientId())) {
            return accountInfoModel.getClientId();
        }
        return clientId;
    }

    private String buildDepartAccountTreeCacheKey(String currentClientId) {
        return DEPART_ACCOUNT_TREE_CACHE_KEY_PREFIX + currentClientId;
    }

    private String buildDepartAccountSubTreeCacheKey(String currentClientId, String deptId) {
        return DEPART_ACCOUNT_SUBTREE_CACHE_KEY_PREFIX + currentClientId + ":" + deptId;
    }

    private List<InsSysDepartVo> getCachedDepartAccountTree(String currentClientId) {
        List<InsSysDepartVo> departAccountTree = departAccountTreeCache.get(buildDepartAccountTreeCacheKey(currentClientId));
        if (departAccountTree != null) {
            return departAccountTree;
        }
        if (Objects.equals(currentClientId, clientId)) {
            return departAccountTreeCache.get(DEPART_ACCOUNT_TREE_CACHE_KEY);
        }
        return null;
    }

    private void cacheDepartAccountTree(String currentClientId, List<InsSysDepartVo> departAccountTree) {
        String cacheKey = buildDepartAccountTreeCacheKey(currentClientId);
        departAccountTreeCache.put(cacheKey, departAccountTree);
        if (Objects.equals(currentClientId, clientId)) {
            departAccountTreeCache.put(DEPART_ACCOUNT_TREE_CACHE_KEY, departAccountTree);
        }
    }

    private List<InsSysDepartVo> getCachedDepartAccountSubtree(String currentClientId, String deptId) {
        if (ObjectUtils.isEmpty(deptId)) {
            return null;
        }
        List<InsSysDepartVo> departAccountSubtree = departAccountTreeCache.get(buildDepartAccountSubTreeCacheKey(currentClientId, deptId));
        if (CollUtil.isEmpty(departAccountSubtree)) {
            return null;
        }
        return copyDepartTreeList(departAccountSubtree);
    }

    private void cacheDepartAccountSubtree(String currentClientId, String deptId, List<InsSysDepartVo> departAccountSubtree) {
        if (ObjectUtils.isEmpty(deptId) || CollUtil.isEmpty(departAccountSubtree)) {
            return;
        }
        departAccountTreeCache.put(buildDepartAccountSubTreeCacheKey(currentClientId, deptId), departAccountSubtree);
    }

    private InsSysDepartVo copyDepartSubtree(InsSysDepartVo depart) {
        if (depart == null) {
            return null;
        }

        InsSysDepartVo departCopy = copyDepartNode(depart);
        if (CollUtil.isNotEmpty(depart.getAccount())) {
            departCopy.setAccount(copyAccountList(depart.getAccount()));
        }
        if (CollUtil.isEmpty(depart.getChild())) {
            return departCopy;
        }

        List<InsSysDepartVo> childDepartCopyList = depart.getChild().stream()
                .map(this::copyDepartSubtree)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(childDepartCopyList)) {
            departCopy.setChild(childDepartCopyList);
        }
        return departCopy;
    }

    private List<InsSysDepartVo> copyDepartTreeList(List<InsSysDepartVo> departList) {
        if (CollUtil.isEmpty(departList)) {
            return Collections.emptyList();
        }
        return departList.stream()
                .map(this::copyDepartSubtree)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<InsSysDepartVo> buildDepartAccountTreeByDeptIdFromCache(String currentClientId, String deptId) {
        List<InsSysDepartVo> cachedDepartAccountTree = getCachedDepartAccountTree(currentClientId);
        if (CollUtil.isEmpty(cachedDepartAccountTree) || ObjectUtils.isEmpty(deptId)) {
            return Collections.emptyList();
        }

        List<InsSysDepartVo> departPath = new ArrayList<>();
        for (InsSysDepartVo depart : cachedDepartAccountTree) {
            if (buildDepartPath(depart, deptId, departPath)) {
                return Collections.singletonList(copyDepartPathTree(departPath));
            }
        }
        return Collections.emptyList();
    }

    private boolean buildDepartPath(InsSysDepartVo currentDepart, String deptId, List<InsSysDepartVo> departPath) {
        if (currentDepart == null || ObjectUtils.isEmpty(deptId) || departPath == null) {
            return false;
        }

        departPath.add(currentDepart);
        if (deptId.equals(currentDepart.getId())) {
            return true;
        }

        List<InsSysDepartVo> childDepartList = currentDepart.getChild();
        if (CollUtil.isNotEmpty(childDepartList)) {
            for (InsSysDepartVo childDepart : childDepartList) {
                if (buildDepartPath(childDepart, deptId, departPath)) {
                    return true;
                }
            }
        }

        departPath.remove(departPath.size() - 1);
        return false;
    }

    private InsSysDepartVo copyDepartPathTree(List<InsSysDepartVo> departPath) {
        if (CollUtil.isEmpty(departPath)) {
            return null;
        }

        InsSysDepartVo currentDepart = copyDepartSubtree(departPath.get(departPath.size() - 1));
        for (int i = departPath.size() - 2; i >= 0; i--) {
            InsSysDepartVo parentDepart = copyDepartNode(departPath.get(i));
            parentDepart.setChild(Collections.singletonList(currentDepart));
            currentDepart = parentDepart;
        }
        return currentDepart;
    }

    /**
     * @param accountInfoModel
     * @return com.voc.service.common.model.UserModel
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/4 15:36
     * @描述 用户数据组装
     **/
    private UserModel userDataAssembly(InsAccountInfoModel accountInfoModel) {
        final String appId = ServiceContextHolder.getSystemId();
        UserModel userModel = UserModel.builder()
                .username(accountInfoModel.getAccountName())
                .password(accountInfoModel.getAccountPwd())
                .firstname(accountInfoModel.getUserName())
                .lastname(accountInfoModel.getUserName())
                .clientId(accountInfoModel.getClientId())
                .type(accountInfoModel.getLoginType())
                .phone(accountInfoModel.getContact())
                .enabled(!"0".equalsIgnoreCase(accountInfoModel.getStatus()) && ("1".equalsIgnoreCase(accountInfoModel.getStatus())))
                .build();
        if (ObjectUtils.isNotEmpty(accountInfoModel.getExpiryDate())) {
            //设置账号有效期
            //校验有效期中是否包含时分秒
            boolean match = ReUtil.isMatch(PatternPool.TIME, accountInfoModel.getExpiryDate());
            String expiry = accountInfoModel.getExpiryDate();
            LocalDateTime dateTime;
            if (match) {
                Pattern pattern = PatternPool.TIME;
                Matcher matcher = pattern.matcher(accountInfoModel.getExpiryDate());
                expiry = matcher.replaceAll("23:59:59");
                dateTime = LocalDateTime.parse(expiry, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } else {
                LocalTime localTime = LocalTime.of(23, 59, 59);
                LocalDate localDate = LocalDate.parse(expiry, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                dateTime = LocalDateTime.of(localDate, localTime);
            }

            userModel.setExpireDate(dateTime);
        }else{
            userModel.setExpireDate(LocalDateTime.of(2099, 12, 31, 23, 59, 59));
        }
        userModel.setAdmin("0");
        userModel.setAppId(appId);
        return userModel;
    }

    /**
     * @param accountInfoModel
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/4 15:35
     * @描述 必填项校验
     **/
    private void checkParameter(InsAccountInfoModel accountInfoModel) {
        Assert.hasLength(accountInfoModel.getAccountName(), "账号名称不允许为空");
        Assert.hasLength(accountInfoModel.getRoleId(), "账号角色不能为空");
        Assert.hasLength(accountInfoModel.getClientId(), "客户ID不能为空");
        Assert.hasLength(accountInfoModel.getStatus(), "停用/启用状态 不允许为空");
        Assert.isTrue(Integer.parseInt(accountInfoModel.getStatus()) >= 0
                && Integer.parseInt(accountInfoModel.getStatus()) <= 1, "状态码无效");
        if (ObjectUtils.isNotEmpty(accountInfoModel.getExpiryDate())) {
            try {
                DateUtil.parse(accountInfoModel.getExpiryDate());
            } catch (Exception e) {
                log.error("日期校验异常:{}", e.getMessage());
                Assert.isNull(accountInfoModel.getExpiryDate(), "有效期格式不正确");
            }
        }
    }

}
