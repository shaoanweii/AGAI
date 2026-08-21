package com.voc.service.components.milvus.service;

import com.voc.service.common.util.ServiceContextHolder;
import io.milvus.client.MilvusServiceClient;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.exception.ParamException;
import io.milvus.grpc.DataType;
import io.milvus.grpc.MutationResult;
import io.milvus.grpc.SearchResults;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.DropCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.param.dml.DeleteParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.response.MutationResultWrapper;
import io.milvus.response.SearchResultsWrapper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @创建者: fanrong
 * @创建时间: 2024/8/2 下午3:24
 * @描述:
 **/
//@Component
public class MilvusService {
    private static final Logger log = LoggerFactory.getLogger(MilvusService.class);
    //    @Autowired
    MilvusServiceClient milvusServiceClient;

    public List<Map<?, ?>> search(List<Float> list, String collectionName, List<String> fieldNames, String vectorFieldName, Integer topK) {
        if (ObjectUtils.isEmpty(list)) {
            log.debug("Embedding 值为空,放弃本次查询");
            return List.of();
        }
        Assert.isTrue(ObjectUtils.isNotEmpty(vectorFieldName), "vectorFieldName不允许为空");
        Assert.isTrue(ObjectUtils.isNotEmpty(collectionName), "collectionName不允许为空");
        //构建向量库查询对象
        SearchParam.Builder builder = SearchParam.newBuilder()
                .withCollectionName(collectionName)
                .withTopK(topK)
                .withVectors(Arrays.asList(list))
                .withConsistencyLevel(ConsistencyLevelEnum.BOUNDED)
                .withVectorFieldName(vectorFieldName);
        if (ObjectUtils.isNotEmpty(fieldNames)) {
            builder.withOutFields(fieldNames);
        }
        if (ObjectUtils.isEmpty(this.milvusServiceClient)) {
            this.milvusServiceClient = ServiceContextHolder.getApplicationContext().getBean(MilvusServiceClient.class);
        }
        long start = System.currentTimeMillis();

        R<SearchResults> search = milvusServiceClient.search(
                builder.build()
        );
        long end = System.currentTimeMillis();
//        log.info("异步查询向量库耗时:{}", TimeUnit.MILLISECONDS.toSeconds(end-start)>0?TimeUnit.MILLISECONDS.toSeconds(end-start)+"秒":(end-start)+"毫秒");
        log.info("同步查询向量库耗时:{}", TimeUnit.MILLISECONDS.toSeconds(end - start) > 0 ? TimeUnit.MILLISECONDS.toSeconds(end - start) + "秒" : (end - start) + "毫秒");
        if (ObjectUtils.isEmpty(search.getData())) {
            return List.of();
        }
        //获取向量库查询结果并转换为VoiceClipResultData
        SearchResultsWrapper wrapper = new SearchResultsWrapper(search.getData().getResults());
        List<Map<?, ?>> collect = wrapper.getRowRecords().stream().map(e -> {
            Map<String, Object> fieldValues = e.getFieldValues();
            Map<?, ?> build = new HashMap<String, Object>();
            return build;
        }).collect(Collectors.toList());

        return collect;
    }
    public String saveMilvusData(String vectorId, String opinion, List<Float> embeddingList, String collectionName) {
        List<String> opinions = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        opinions.add(opinion);
        ids.add(vectorId);
        // 创建字段列表
        List<InsertParam.Field> fields = new ArrayList<>();
        fields.add(new InsertParam.Field("id", ids));
        fields.add(new InsertParam.Field("opinion", opinions));
        List<List<Float>> list=new ArrayList<>();list.add(embeddingList);
        fields.add(new InsertParam.Field("embedding", list));
        // 如果milvusServiceClient为空，则从Spring容器中获取
        if (ObjectUtils.isEmpty(this.milvusServiceClient)) {
            this.milvusServiceClient = ServiceContextHolder.getApplicationContext().getBean(MilvusServiceClient.class);
        }
        // 创建插入参数
        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName(collectionName)
                .withFields(fields)
                .build();

        // 插入数据并获取插入结果
        MutationResult mutationResult = milvusServiceClient.insert(insertParam).getData();
        // 检查mutationResult是否为空
        if (mutationResult == null) {
            throw new RuntimeException("Mutation result is null");
        }
        // 包装mutationResult
        MutationResultWrapper resultWrapper = new MutationResultWrapper(mutationResult);
        // 获取插入的 ID 列表
        List<String> insertedIds = resultWrapper.getStringIDs();

        // 检查insertedIds是否为空或空列表
        if (insertedIds == null || insertedIds.isEmpty()) {
            throw new RuntimeException("No IDs were returned after insertion");
        }

        // 返回第一个插入的 ID（假设你只需要一个 ID）
        return insertedIds.get(0).toString();
    }
    /**
     * 创建一个新的集合
     *
     * @param collectionName 集合名称
     * @param dimension      向量维度
     * @return 创建结果
     */
    public R<RpcStatus> createCollection(String collectionName, int dimension,String description) {
        R<RpcStatus> response = null;
        try {
            // 定义字段
            FieldType idField = FieldType.newBuilder()
                    .withName("id")
                    .withDataType(DataType.VarChar)
                    .withPrimaryKey(true)
                    .withAutoID(false)
                    .withMaxLength(50)
                    .build();

            FieldType embedding = FieldType.newBuilder()
                    .withName("embedding")
                    .withDataType(DataType.FloatVector)
                    .withDimension(dimension)
                    .build();
            FieldType opinion = FieldType.newBuilder()
                    .withName("opinion")
                    .withDataType(DataType.VarChar)
                    .withMaxLength(1024)
                    .build();

            // 创建集合参数
            CreateCollectionParam createCollectionReq = CreateCollectionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withDescription(description)
                    .withShardsNum(2)
                    .addFieldType(idField)
                    .addFieldType(embedding)
                    .addFieldType(opinion)
                    .build();

            // 创建集合
            response = milvusServiceClient.createCollection(createCollectionReq);
            if (response.getStatus() != R.Status.Success.getCode()) {
                System.err.println("Failed to create collection: " + response.getMessage());
            }

            // 创建索引
            CreateIndexParam createIndexParam = CreateIndexParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withFieldName("embedding")
                    .withIndexName("embedding_index")
                    .withIndexType(IndexType.IVF_FLAT)
                    .withMetricType(MetricType.COSINE)
                    .withExtraParam("{\"nlist\":12}")
                    .build();

            R<RpcStatus> indexResponse = milvusServiceClient.createIndex(createIndexParam);
            if (indexResponse.getStatus() != R.Status.Success.getCode()) {
                System.err.println("Failed to create index: " + indexResponse.getMessage());
                return indexResponse;
            }
            // 加载集合
            LoadCollectionParam loadCollectionParam = LoadCollectionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .build();

            R<RpcStatus> loadResponse = milvusServiceClient.loadCollection(loadCollectionParam);
            if (loadResponse.getStatus() != R.Status.Success.getCode()) {
                System.err.println("Failed to load collection: " + loadResponse.getMessage());
                return loadResponse;
            }
        } catch (ParamException e) {
            throw new RuntimeException(e);
        }


        return response;
    }

    public int saveMilvusBatchData(List<Triple<String, List<Float>, String>> list1,String collectionName) {
        // 创建字段列表
        List<InsertParam.Field> fields = new ArrayList<>();

        // 提取 id 字段
        List<String> idList = list1.stream().map(Triple::getLeft).collect(Collectors.toList());
        fields.add(new InsertParam.Field("id", idList));

        // 提取 opinion 字段
        List<String> textList = list1.stream().map(Triple::getRight).collect(Collectors.toList());
        fields.add(new InsertParam.Field("opinion", textList));

        // 提取 embedding 字段
        List<List<Float>> embeddingList = list1.stream().map(Triple::getMiddle).collect(Collectors.toList());
        fields.add(new InsertParam.Field("embedding", embeddingList));

        // 如果 milvusServiceClient 为空，则从 Spring 容器中获取
        if (ObjectUtils.isEmpty(this.milvusServiceClient)) {
            ApplicationContext context = ServiceContextHolder.getApplicationContext();
            if (context != null) {
                this.milvusServiceClient = context.getBean(MilvusServiceClient.class);
            } else {
                throw new IllegalStateException("Spring ApplicationContext is not available.");
            }
        }
        // 创建插入参数
        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName(collectionName)
                .withFields(fields)
                .build();


        // 插入数据并获取插入结果
        MutationResult mutationResult;
        try {
            mutationResult = milvusServiceClient.insert(insertParam).getData();
        } catch (Exception e) {
            log.error("Failed to insert data into Milvus", e);
            e.printStackTrace();
            throw new RuntimeException("Failed to insert data into Milvus", e);
        }
        // 检查 mutationResult 是否为空
        if (mutationResult == null) {
            throw new RuntimeException("Mutation result is null");
        }
        // 包装 mutationResult
        MutationResultWrapper resultWrapper = new MutationResultWrapper(mutationResult);
        return resultWrapper.getStringIDs().size();
    }

    public void removeCollection(String collectionName) {
        milvusServiceClient.dropCollection(DropCollectionParam.newBuilder().withCollectionName(collectionName).build());
    }

    public void delete(String collectionName, String id) {
        // 构建 DeleteParam 对象
        DeleteParam deleteParam = DeleteParam.newBuilder()
                .withCollectionName(collectionName)
                .withExpr("id == \"" + id + "\"") // 假设 id 是字符串类型
                .build();

        // 调用 delete 方法
        milvusServiceClient.delete(deleteParam);
    }
}
