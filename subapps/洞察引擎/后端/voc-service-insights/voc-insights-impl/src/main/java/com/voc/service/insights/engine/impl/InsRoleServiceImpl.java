package com.voc.service.insights.engine.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.model.UserModel;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.api.IInsMenuPermissionService;
import com.voc.service.insights.engine.api.IInsRoleRelationPermissionService;
import com.voc.service.insights.engine.api.IInsRoleService;
import com.voc.service.insights.engine.api.IInsUserRoleService;
import com.voc.service.insights.engine.dao.InsAccountInfoDao;
import com.voc.service.insights.engine.entity.InsRoleEntity;
import com.voc.service.insights.engine.mapper.InsRoleMapper;
import com.voc.service.insights.engine.model.*;
import com.voc.service.insights.engine.vo.*;
import com.voc.service.security.api.clients.ISecurityServiceClient;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class InsRoleServiceImpl extends ServiceImpl<InsRoleMapper, InsRoleEntity> implements IInsRoleService {
    private static final Logger log = LoggerFactory.getLogger(InsRoleServiceImpl.class);

    @Resource
    private IInsMenuPermissionService iInsMenuPermissionService;

    @Resource
    private IInsRoleRelationPermissionService iInsRoleRelationPermissionService;

    @Resource
    private IInsUserRoleService iInsUserRoleService;

    private final String clientId = "764547797eb2e192763f5334028d49c9";

    @Resource
    ISecurityServiceClient securityServiceClient;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @SwitchClientDS(objectAttribute = "model.clientId")
    public PageInfo queryRoleList(InsRoleQueryModel model) {
        PageHelper.startPage(model.getPageNum(), model.getPageSize());
        List<RoleListVo> roleListVos = this.baseMapper.queryRoleList(model);
        if (CollUtil.isEmpty(roleListVos)) {
            return new PageInfo();
        }
//        for (RoleListVo roleVo : roleListVos) {
//            if (StringUtils.isNotBlank(roleVo.getUserIdsStr())) {
//                // 将 userIdsStr 转换为 userIds 列表
//                List<String> userIds = Arrays.stream(roleVo.getUserIdsStr().split(","))
//                        .map(String::trim)
//                        .filter(StringUtils::isNotBlank)
//                        .collect(Collectors.toList());
//                roleVo.setUserIds(userIds);
//            } else {
//                roleVo.setUserIds(new ArrayList<>());
//            }
//        }
//        List<String> allUserIds = roleListVos.stream()
//                .filter(vo -> vo.getUserIds() != null)  // 过滤掉 null 的 userIds
//                .flatMap(vo -> vo.getUserIds().stream())  // 展开为单个 userId 流
//                .distinct()  // 去重（可选）
//                .collect(Collectors.toList());
//        if (CollUtil.isNotEmpty(allUserIds)) {
//            UserModel userModel = new UserModel();
//            userModel.setClientId(clientId);
//            userModel.setUserIds(allUserIds);
//            Result<List<UserModel>> userResult = securityServiceClient.findByUserId(userModel);
//            List<UserModel> userModelList = userResult.getResult();
//
//            // 创建 userId 到 userName 的映射
//            Map<String, String> userIdToNameMap = new HashMap<>();
//            if (CollUtil.isNotEmpty(userModelList)) {
//                userIdToNameMap = userModelList.stream()
//                        .collect(Collectors.toMap(UserModel::getUserId, UserModel::getUsername, (k1, k2) -> k1));
//            }
//            // 为每条 RoleReportListVo 设置 userName 列表
//            for (RoleListVo roleVo : roleListVos) {
//                if (roleVo.getUserIds() != null && !roleVo.getUserIds().isEmpty()) {
//                    List<String> userNames = roleVo.getUserIds().stream()
//                            .map(userIdToNameMap::get)
//                            .filter(Objects::nonNull)
//                            .collect(Collectors.toList());
//                    roleVo.setUserName(userNames);
//                } else {
//                    roleVo.setUserName(new ArrayList<>());
//                }
//            }
//        }
        return new PageInfo<>(roleListVos);
    }

    @Override
    @SwitchClientDS(objectAttribute = "model.clientId")
    public Result<?> queryRoleALlList(InsRoleQueryModel model) {
        QueryWrapper<InsRoleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("enabled", "1");
        queryWrapper.orderByAsc("create_time");
        List<InsRoleEntity> roleEntityList = this.list(queryWrapper);
        return Result.OK(roleEntityList);
    }

    @Override
    @SwitchClientDS(objectAttribute = "model.clientId")
    public PageInfo getUserRoleList(InsRoleQueryModel model) {
        // 1. 1. 获取角色列表（数据库查询，耗时取决于索引优化）
        long startTime = System.currentTimeMillis();
        List<RoleListVo> roleListVos = baseMapper.allQueryRoleList(model);

        // 2. 处理角色关联的用户ID（拆分为当前角色和非当前角色的用户ID集合）
        Set<String> currentRoleUserIds = new HashSet<>();  // 当前角色已配置的用户ID
        Set<String> otherRolesUserIds = new HashSet<>();   // 非当前角色已配置的用户ID

        String targetRoleId = model.getRoleId();
        if (com.alibaba.cloud.commons.lang.StringUtils.isNotBlank(targetRoleId)) {
            // 2.1 提取当前角色的用户ID（优化空指针和Optional安全处理）
            RoleListVo currentRole = roleListVos.stream()
                    .filter(role -> targetRoleId.equals(role.getRoleId()))
                    .findAny()
                    .orElse(null);
            currentRoleUserIds = splitToUserIdSet(currentRole != null ? currentRole.getUserIdsStr() : null);

            // 2.2 提取非当前角色的用户ID（批量处理）
            for (RoleListVo role : roleListVos) {
                if (!targetRoleId.equals(role.getRoleId())) {  // 排除当前角色
                    otherRolesUserIds.addAll(splitToUserIdSet(role.getUserIdsStr()));
                }
            }
        }

        // 3. 获取用户列表（远程接口/数据库，核心优化点：建议缓存或按需查询）
        List<UserModel> userModelList = getFindByUserIdCache();
        if (CollUtil.isEmpty(userModelList)){
            return PageInfo.emptyPageInfo();

        }
        // 3.1 预过滤无效用户（提前排除userId为空的数据，减少后续处理）
        List<UserModel> validUsers = userModelList.stream()
                .filter(u -> com.alibaba.cloud.commons.lang.StringUtils.isNotBlank(u.getUserId()))
                .collect(Collectors.toList());
        int validUserCount = validUsers.size();


        // 4. 一次循环完成用户转换+分类（核心优化：合并循环+预初始化集合容量）
        // 按linkType拆分两个列表，避免后续排序开销（因linkType只有0和1）
        List<InsUserRoleListVo> linkType1List = new ArrayList<>();  // 预估容量
        List<InsUserRoleListVo> linkType0List = new ArrayList<>();  // 预估容量

        for (UserModel user : validUsers) {
            String userId = user.getUserId();
            InsUserRoleListVo vo = new InsUserRoleListVo();
            vo.setUserId(userId);
            vo.setUserName(user.getFirstname());
            vo.setEmployeeId(user.getEmployeeId());
            vo.setDepartName("");  // 若有部门信息可在此补充（建议从UserModel中提取）

            // 分类添加（基于用户ID所在集合）
            if (currentRoleUserIds.contains(userId)) {
                vo.setLinkType(Integer.valueOf(1));
                linkType1List.add(vo);
            } else if (!otherRolesUserIds.contains(userId)) {
                vo.setLinkType(Integer.valueOf(0));
                linkType0List.add(vo);
            }
            // 其他情况（在非当前角色中配置）：不添加
        }


        // 5. 搜索过滤（优化：先过滤再合并，减少数据量）
        String keyword = model.getSearchKeyword();
        if (com.alibaba.cloud.commons.lang.StringUtils.isNotEmpty(keyword)) {
            linkType1List = filterByKeyword(linkType1List, keyword);
            linkType0List = filterByKeyword(linkType0List, keyword);
        }



        // 6. 排序合并（优化：用列表合并替代快排，O(n)复杂度）
        List<InsUserRoleListVo> allVos = new ArrayList<>();
        Integer sort = model.getSort();
        if (sort == 2) {
            // 倒序：linkType=1在前，0在后
            allVos.addAll(linkType1List);
            allVos.addAll(linkType0List);
        } else {
            // 默认/正序：linkType=0在前，1在后（sort=1或null）
            allVos.addAll(linkType0List);
            allVos.addAll(linkType1List);
        }


        // 7. 分页处理（基于最终列表计算）
        int pageNum = Optional.ofNullable(model.getPageNum()).orElse(Integer.valueOf(1));
        int pageSize = Optional.ofNullable(model.getPageSize()).orElse(Integer.valueOf(10));
        pageNum = Math.max(1, pageNum);
        pageSize = Math.max(1, pageSize);


        int total = allVos.size();
        // 计算分页索引（避免越界）
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<InsUserRoleListVo> pageData = start >= total
                ? Collections.emptyList()
                : new ArrayList<>(allVos.subList(start, end));  // 转为新列表，避免视图引用


        // 打印耗时（仅调试用）
        long cost = System.currentTimeMillis() - startTime;
        System.out.println("接口总耗时：" + cost + "ms");
        PageInfo<InsUserRoleListVo> userRoleListVoPageInfo = new PageInfo<>(pageData);
        userRoleListVoPageInfo.setTotal(total);
        return userRoleListVoPageInfo;
    }


    private Set<String> splitToUserIdSet(String userIdsStr) {
        if (com.alibaba.cloud.commons.lang.StringUtils.isEmpty(userIdsStr)) {
            return Collections.emptySet();
        }
        // 拆分+去重+过滤空字符串
        return Arrays.stream(userIdsStr.split(","))
                .map(String::trim)
                .filter(com.alibaba.cloud.commons.lang.StringUtils::isNotBlank)
                .collect(Collectors.toSet());
    }


    public  List<UserModel> getFindByUserIdCache() {
        String cacheKey = "cqca:ins:security_user";
        List<UserModel> ruleList = getFromRedis(cacheKey);
        if (ruleList != null) {
            return ruleList;
        }
        UserModel userQuery = new UserModel();
        userQuery.setClientId(clientId);
        userQuery.setAppId(ServiceContextHolder.getSystemId());
        Result<List<UserModel>> userResult = securityServiceClient.findByUserId(userQuery);
        List<UserModel> userModelList = userResult.getResult();
        if (CollUtil.isEmpty(userModelList)) {
            return new ArrayList<>();
        }
        if (ruleList == null) {
            stringRedisTemplate.opsForValue().set(cacheKey, JSONArray.toJSONString(userModelList), 7, TimeUnit.DAYS);
        }
        return userModelList;
    }

    private List<UserModel> getFromRedis(String cacheKey) {
        String cachedData = stringRedisTemplate.opsForValue().get(cacheKey);
        if (com.alibaba.cloud.commons.lang.StringUtils.isNotEmpty(cachedData)) {
            List<UserModel> list = JSONArray.parseArray(cachedData, UserModel.class);
//            List<UserModel> list = JSONUtil.toList(cachedData, UserModel.class);
            return list;
        }
        return null;
    }

    private List<InsUserRoleListVo> filterByKeyword(List<InsUserRoleListVo> vos, String keyword) {
        if (CollUtil.isEmpty(vos)) {
            return Collections.emptyList();
        }
        return vos.stream()
                .filter(vo ->
                        (vo.getUserName() != null && vo.getUserName().contains(keyword)) ||
                                (vo.getEmployeeId() != null && vo.getEmployeeId().contains(keyword))
                )
                .collect(Collectors.toList());
    }

    @Override
    @SwitchClientDS(objectAttribute = "model.clientId")
    public List<RoleAuthTree> queryMenuPermissionList(InsRoleQueryModel model) {

        List<RoleAuthTree> roleAuthTreeList = new ArrayList<>();
        List<InsRolePermissionVo> permissionModelList = iInsMenuPermissionService.getMenuPermission();
        if (CollectionUtil.isEmpty(permissionModelList)) {
            return roleAuthTreeList;
        }
        permissionModelList = permissionModelList.stream().filter(p -> p.getFilterStatus() == 0).collect(Collectors.toList());
        List<String> permissionIdList = model.getPermissionIdList();
        for (InsRolePermissionVo menuPermissionEntity : permissionModelList) {
            if ("0".equals(menuPermissionEntity.getParentId())) {
                RoleAuthTree roleAuthTree = new RoleAuthTree();
                roleAuthTree.setId(menuPermissionEntity.getId());
                roleAuthTree.setIcon(menuPermissionEntity.getIcon());
                roleAuthTree.setChecked(permissionIdList.contains(menuPermissionEntity.getId()) || model.getSelectAll() ? Boolean.TRUE : Boolean.FALSE);
                roleAuthTree.setName(menuPermissionEntity.getName());
                roleAuthTree.setCheckButton(Boolean.FALSE);
                roleAuthTree.setSort(menuPermissionEntity.getSortNo());
                roleAuthTree.setPid(menuPermissionEntity.getParentId());
                roleAuthTree.setPath(menuPermissionEntity.getHtmlUri());
                roleAuthTree.setPermissionKey(menuPermissionEntity.getPermissionKey());
                roleAuthTreeList.add(roleAuthTree);
            }
        }
        roleAuthTreeList.sort(Comparator.comparing(RoleAuthTree::getSort,Comparator.nullsLast(Comparator.naturalOrder())));
        for (RoleAuthTree tree : roleAuthTreeList) {
            this.roleTree(tree, permissionModelList, permissionIdList, model);
        }
        return roleAuthTreeList;
    }

    /**
     * 递归
     */
    private void roleTree(RoleAuthTree tree, List<InsRolePermissionVo> permissionModelList, List<String> permissionIdList, InsRoleQueryModel model) {
        List<RoleAuthTree> scouts = new ArrayList<>();
        for (InsRolePermissionVo permissionEntity : permissionModelList) {
            if (Objects.equals(tree.getId(), permissionEntity.getParentId())) {
                RoleAuthTree roleAuthTree = new RoleAuthTree();
                roleAuthTree.setId(permissionEntity.getId());
                roleAuthTree.setIcon(permissionEntity.getIcon());
                roleAuthTree.setChecked(permissionIdList.contains(permissionEntity.getId()) || model.getSelectAll() ? Boolean.TRUE : Boolean.FALSE);
                roleAuthTree.setName(permissionEntity.getName());
                roleAuthTree.setCheckButton(Boolean.valueOf(ObjectUtils.isNotEmpty(permissionEntity.getButtonCode())));
                roleAuthTree.setPath(permissionEntity.getHtmlUri());
                roleAuthTree.setPid(permissionEntity.getParentId());
                roleAuthTree.setSort(permissionEntity.getSortNo());
                roleAuthTree.setPermissionKey(permissionEntity.getPermissionKey());
                roleTree(roleAuthTree, permissionModelList, permissionIdList, model);
                scouts.add(roleAuthTree);
                scouts.sort(Comparator.comparing(RoleAuthTree::getSort, Comparator.nullsLast(Comparator.naturalOrder())));
                tree.setChildren(scouts);
            }
        }
    }

    @Override
    @SwitchClientDS(objectAttribute = "model.clientId")
    public Result<?> saveOrUpdateRole(RoleAuthModel model) {
        List<InsRolePermissionVo> permissionModelList = iInsMenuPermissionService.getMenuPermission();
        if (CollectionUtil.isEmpty(permissionModelList)) {
            return Result.error("基础权限没有配置请联系管理员初始化");
        }
        Boolean checkedRoleName = this.checkRoleName(model);
        if (checkedRoleName) {
            return Result.error("角色名称不能重复");
        }
        Map<String, InsRolePermissionVo> permissionModelMap = permissionModelList.stream().collect(Collectors.toMap(InsRolePermissionVo::getId, Function.identity()));
        if (StringUtils.isNotBlank(model.getId())) {
            InsRoleEntity insRoleEntity = this.getById(model.getId());
            if (ObjectUtils.isEmpty(insRoleEntity)) {
                return Result.error("数据有误");
            }
            if (model.getEnabled() == 0) {
                Integer countByRole = iInsUserRoleService.getCountByRole(model.getId());
                if (countByRole > 0) {
                    return Result.error("角色下面已关联" + countByRole + "个账户不能禁用");
                }
            }
            insRoleEntity.setRoleName(model.getRoleName());
            insRoleEntity.setUpdateTime(LocalDateTime.now());
            insRoleEntity.setEnabled(model.getEnabled());
            insRoleEntity.setRemark(ObjectUtils.isNotEmpty(model.getRemark()) ? model.getRemark() : "");
            boolean update = this.updateById(insRoleEntity);
            if (update) {
                iInsRoleRelationPermissionService.delete(insRoleEntity.getId());
                boolean b = insertRoleRelationPermission(model.getPermissionIdList(), permissionModelMap, model.getId());
                if(b){
                    log.info("角色权限保存成功");
                }else{
                    log.info("角色权限保存失败");
                    return Result.OK(Boolean.FALSE);
                }
            }
        } else {
            InsRoleEntity entity = new InsRoleEntity();
            entity.setRoleType(Integer.valueOf(0));
            entity.setRoleName(model.getRoleName());
            entity.setId(IdWorker.getId());
            entity.setEnabled(model.getEnabled());
            entity.setCreateTime(LocalDateTime.now());
            entity.setRemark(ObjectUtils.isNotEmpty(model.getRemark()) ? model.getRemark() : "");
            boolean save = this.save(entity);
            model.setId(entity.getId());
            if (save) {
                boolean b = insertRoleRelationPermission(model.getPermissionIdList(), permissionModelMap, entity.getId());
                if(b){
                    log.info("角色权限保存成功");
                }else{
                    log.info("角色权限保存失败");
                    return Result.OK(Boolean.FALSE);
                }
            }
        }

        if (!CollectionUtil.isEmpty(model.getUserIdList())) {
            List<String> userIdList = model.getUserIdList().stream().distinct().collect(Collectors.toList());
            InsUserRoleModel models = new InsUserRoleModel();
            models.setUserIdList(userIdList);
            models.setRoleId(model.getId());
            iInsUserRoleService.batchSaveOrUpdate(models);
        }
        return Result.OK(Boolean.TRUE);
    }


    public Boolean checkRoleName(RoleAuthModel model) {
        if (ObjectUtils.isEmpty(model.getId())) {
            QueryWrapper<InsRoleEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("role_name", model.getRoleName());
            InsRoleEntity entity = this.baseMapper.selectOne(queryWrapper);
            return Boolean.valueOf(ObjectUtils.isNotEmpty(entity));
        } else {
            InsRoleEntity insRoleEntity = this.getById(model.getId());
            if (insRoleEntity.getRoleName().equals(model.getRoleName())) {
                return Boolean.FALSE;
            } else {
                QueryWrapper<InsRoleEntity> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("role_name", model.getRoleName());
                InsRoleEntity entity = this.baseMapper.selectOne(queryWrapper);
                return Boolean.valueOf(ObjectUtils.isNotEmpty(entity));
            }
        }
    }

    private boolean insertRoleRelationPermission(List<String> permissionIdList, Map<String, InsRolePermissionVo> permissionModelMap, String roleId) {

        List<InsRoleRelationPermissionModel> insRoleRelationPermissionModels = new ArrayList<>();
        for (String permissionId : permissionIdList) {
            InsRoleRelationPermissionModel entity = new InsRoleRelationPermissionModel();
            entity.setRoleId(roleId);
            entity.setId(IdWorker.getId());
            entity.setCreateTime(LocalDateTime.now());
            entity.setPermissionId(permissionId);
            if (permissionModelMap.containsKey(permissionId)) {
                InsRolePermissionVo insRolePermissionModel = permissionModelMap.get(permissionId);
                entity.setPermissionType(insRolePermissionModel.getPermissionType());
                entity.setButtonPermission(insRolePermissionModel.getButtonCode());
            }
            insRoleRelationPermissionModels.add(entity);

        }
        if (CollectionUtil.isNotEmpty(insRoleRelationPermissionModels)) {
            return iInsRoleRelationPermissionService.insertBatch(insRoleRelationPermissionModels) > 0;
        }
        return Boolean.FALSE;
    }

    @Override
    @SwitchClientDS(objectAttribute = "model.clientId")
    public Result<?> queryRoleInfo(RoleInfoQueryModel model) {
        InsRoleEntity insRoleEntity = this.getById(model.getId());
        if (ObjectUtils.isEmpty(insRoleEntity)) {
            return Result.error("数据有误");
        }
        RoleAuthVo roleAuthVo = new RoleAuthVo();
        roleAuthVo.setRoleName(insRoleEntity.getRoleName());
        roleAuthVo.setEnabled(insRoleEntity.getEnabled());
        roleAuthVo.setId(insRoleEntity.getId());
        roleAuthVo.setRemark(ObjectUtils.isNotEmpty(insRoleEntity.getRemark())?insRoleEntity.getRemark():"");

        List<InsRoleRelationPermissionModel> insRoleRelationPermissionModels = iInsRoleRelationPermissionService.queryList(insRoleEntity.getId());
        if (CollectionUtil.isEmpty(insRoleRelationPermissionModels)) {
            return Result.OK(roleAuthVo);
        }
        List<String> permissionList = insRoleRelationPermissionModels.stream().map(InsRoleRelationPermissionModel::getPermissionId).toList();
        InsRoleQueryModel queryModel = new InsRoleQueryModel();
        queryModel.setClientId(model.getClientId());
        queryModel.setPermissionIdList(permissionList);
        List<RoleAuthTree> roleAuthTreeList = this.queryMenuPermissionList(queryModel);
        roleAuthVo.setRoleAuthTreeList(roleAuthTreeList);
        return Result.OK(roleAuthVo);
    }

    @Override
    @SwitchClientDS(objectAttribute = "model.clientId")
    public UserRoleInfoVo queryUserPermission(UserRoleQueryModel model) {

        log.info(">>>>获取菜单权限开始:{}", JSON.toJSONString(model));
        UserRoleInfoVo userRoleInfoVo = new UserRoleInfoVo();
        List<InsRolePermissionVo> allPermissionModelList = iInsMenuPermissionService.getMenuPermission();
        if (CollectionUtil.isEmpty(allPermissionModelList)) {
            log.info("菜单权限信息没有配置");
            return null;
        }
        log.info("isAdmin:{}",model.getAdmin());
        if (!model.getAdmin()) {
            allPermissionModelList = allPermissionModelList.stream().filter(p -> p.getFilterStatus() == 0).collect(Collectors.toList());
            log.info("非全局管理员查询权限");
            final String roleId = iInsUserRoleService.getRoleIdByUserId(model.getUserId(),clientId );
            if (StringUtils.isEmpty(roleId)) {
                log.info("用户没有关联角色信息");
                return null;
            }
            log.trace("getById(roleId)：{}",roleId);
            InsRoleEntity insRoleEntity = this.getById(roleId);
            if (ObjectUtils.isEmpty(insRoleEntity) || insRoleEntity.getEnabled() == 0) {
                log.info("角色信息已禁用或者是空");
                return null;
            }
            log.trace("queryList：{}",insRoleEntity.getId());
            final List<InsRoleRelationPermissionModel> insRoleRelationPermissionModels = iInsRoleRelationPermissionService.queryList(insRoleEntity.getId());
            if (CollectionUtil.isEmpty(insRoleRelationPermissionModels)) {
                log.info("角色关联菜单信息为空");
                return null;
            }
            //通过按钮找所有上级
            List<String> buttonIdList = new ArrayList<>(insRoleRelationPermissionModels.stream().filter(i -> i.getPermissionType() == 1 && ObjectUtils.isNotEmpty(i.getButtonPermission())).map(InsRoleRelationPermissionModel::getPermissionId).toList());
            List<InsRolePermissionVo> insRolePermissionVos = allPermissionModelList.stream().filter(a -> buttonIdList.contains(a.getId())).toList();
            Map<String, List<InsRolePermissionVo>> permissionMap = insRolePermissionVos.stream().collect(Collectors.groupingBy(InsRolePermissionVo::getParentId));
            List<String> buttonPermissionList = new ArrayList<>();
            log.trace("permissionMap：{}", Optional.of(permissionMap.size()));
            for (Map.Entry<String, List<InsRolePermissionVo>> entry : permissionMap.entrySet()) {
                List<String> list = entry.getValue().stream().map(InsRolePermissionVo::getPermissionKey).filter(p -> p.contains("select")).toList();
                if (CollectionUtil.isNotEmpty(list)) {
                    buttonPermissionList.addAll(entry.getValue().stream().map(InsRolePermissionVo::getId).toList());
                } else {
                    log.info("菜单没有查看权限");
                }
            }
            log.trace("buttonPermissionList：{}", Optional.of(buttonPermissionList.size()));
            if (CollectionUtil.isEmpty(buttonPermissionList)) {
                log.info("没有配置查看权限不展示:{}", Optional.of(buttonPermissionList.size()));
                return userRoleInfoVo;
            }
            log.info("按钮id集合:{}", Optional.of(buttonPermissionList.size()));
            allPermissionModelList = iInsMenuPermissionService.getUserMenuPermission(buttonPermissionList);
            if (CollectionUtil.isEmpty(allPermissionModelList)) {
                log.info("菜单权限信息没有配置");
                return null;
            }
        }
        log.trace("allPermissionModelList：{}", Optional.of(allPermissionModelList.size()));
        userRoleInfoVo.setInsRolePermissionVos(allPermissionModelList);
        if (model.getTree()) {
            List<InsRolePermissionVo> rolePermissionVoList = allPermissionModelList.stream().filter(p -> p.getPermissionType() == 2).toList();
            List<RoleAuthListVo> roleAuthListVoList = new ArrayList<>();
            for (InsRolePermissionVo menuPermissionEntity : rolePermissionVoList) {
                if ("0".equals(menuPermissionEntity.getParentId())) {
                    RoleAuthListVo roleAuthTree = new RoleAuthListVo();
                    roleAuthTree.setId(menuPermissionEntity.getId());
                    roleAuthTree.setIcon(menuPermissionEntity.getIcon());
                    roleAuthTree.setName(menuPermissionEntity.getName());
                    roleAuthTree.setPath(menuPermissionEntity.getHtmlUri());
                    roleAuthTree.setApiPath(menuPermissionEntity.getApiUrl());
                    roleAuthTree.setPid(menuPermissionEntity.getParentId());
                    roleAuthTree.setSort(menuPermissionEntity.getSortNo());
                    roleAuthTree.setPermissionKey(menuPermissionEntity.getPermissionKey());
                    roleAuthListVoList.add(roleAuthTree);
                }
            }
            roleAuthListVoList.sort(Comparator.comparing(RoleAuthListVo::getSort,Comparator.nullsLast(Comparator.naturalOrder())));
            for (RoleAuthListVo tree : roleAuthListVoList) {
                this.roleAuthTree(tree, rolePermissionVoList);
            }
            log.trace("roleAuthListVoList：{}", Optional.of(roleAuthListVoList.size()));
            userRoleInfoVo.setRoleAuthListVoList(roleAuthListVoList);
        }else{
            log.trace("model.getTree()：{}",model.getTree());
        }
        return userRoleInfoVo;
    }

    @Override
    @SwitchClientDS(objectAttribute = "model.clientId")
    public Result<?> deleteRole(RoleInfoQueryModel model) {
        Assert.hasLength(model.getId(), "角色ID不允许为空");
        int i = this.baseMapper.deleteById(model.getId());
        if (i > 0) {
           log.info("删除成功");
        }else{
            return Result.OK(Boolean.FALSE);
        }
        iInsUserRoleService.deleteRoleByRoleId(InsUserRoleModel.builder().clientId(model.getClientId()).roleId(model.getId()).build());
        return Result.OK(Boolean.FALSE);
    }

    @Override
    @SwitchClientDS(objectAttribute = "model.clientId")
    public RoleAuthVo getRoleInfo(RoleInfoQueryModel model) {
        Assert.hasLength(model.getId(), "角色ID不允许为空");
        final InsRoleEntity insRoleEntity = this.baseMapper.selectById(model.getId());
        if(ObjectUtils.isEmpty(insRoleEntity)){
            log.info("角色信息不存在");
            return null;
        }
        RoleAuthVo roleAuthVo = new RoleAuthVo();
        roleAuthVo.setRoleName(insRoleEntity.getRoleName());
        roleAuthVo.setEnabled(insRoleEntity.getEnabled());
        roleAuthVo.setId(insRoleEntity.getId());
        roleAuthVo.setRemark(ObjectUtils.isNotEmpty(insRoleEntity.getRemark())?insRoleEntity.getRemark():"");

        return roleAuthVo;
    }


    /**
     * 递归
     */
    private void roleAuthTree(RoleAuthListVo tree, List<InsRolePermissionVo> permissionVoList) {
        List<RoleAuthListVo> scouts = new ArrayList<>();
        for (InsRolePermissionVo permissionEntity : permissionVoList) {
            if (Objects.equals(tree.getId(), permissionEntity.getParentId())) {
                RoleAuthListVo roleAuthTree = new RoleAuthListVo();
                roleAuthTree.setId(permissionEntity.getId());
                roleAuthTree.setIcon(permissionEntity.getIcon());
                roleAuthTree.setName(permissionEntity.getName());
                roleAuthTree.setPath(permissionEntity.getHtmlUri());
                roleAuthTree.setApiPath(permissionEntity.getApiUrl());
                roleAuthTree.setPid(permissionEntity.getParentId());
                roleAuthTree.setPermissionKey(permissionEntity.getPermissionKey());
                roleAuthTree.setSort(permissionEntity.getSortNo());
                roleAuthTree(roleAuthTree, permissionVoList);
                scouts.add(roleAuthTree);
                scouts.sort(Comparator.comparing(RoleAuthListVo::getSort, Comparator.nullsLast(Comparator.naturalOrder())));
                tree.setChildren(scouts);
            }
        }
    }
}
