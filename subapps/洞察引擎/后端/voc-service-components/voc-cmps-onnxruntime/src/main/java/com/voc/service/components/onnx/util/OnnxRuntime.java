//package com.voc.service.components.onnx.util;
//
//import ai.onnxruntime.OnnxTensor;
//import ai.onnxruntime.OrtException;
//import ai.onnxruntime.OrtSession;
//import com.voc.service.components.onnx.bertTokenizer.BertTokenizer;
//import org.apache.commons.lang3.ObjectUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.CopyOnWriteArrayList;
//
///**
// * @创建者: fanrong
// * @创建时间: 2024/8/2 下午3:12
// * @描述:
// **/
//@Component
//public class OnnxRuntime {
//    private static final Logger log = LoggerFactory.getLogger(OnnxRuntime.class);
//    @Autowired
//    private OrtSession session;
//
//    public OnnxRuntime(OrtSession session) {
//        this.session = session;
//    }
//
//    public List<Float> getEmbedding(final List<String> list) {
//        if (ObjectUtils.isEmpty(list)) {
//            return List.of();
//        }
//        //获取分词器
//        BertTokenizer bertTokenizer = BertTokenizer.getInstance();
//        final Map<String, OnnxTensor> stringOnnxTensorMap;
//        try {
//            //对文本进行分词
//            stringOnnxTensorMap = bertTokenizer.tokenizeOnnxTensor(list);
//        } catch (OrtException e) {
//            log.error("分词器分词异常:{}", e);
//            throw new RuntimeException("分词器分词异常");
//        }
//        final OrtSession.Result run;
//        try {
//            //onnxRuntime模型语义相似度计算
//            run = session.run(stringOnnxTensorMap);
//        } catch (OrtException e) {
//            log.error("onnx模型语义相似度计算异常:{}", e);
//            throw new RuntimeException("onnx模型语义相似度计算异常");
//        }
//        final float[][][] result;
//        try {
//            result = (float[][][]) run.get(0).getValue();
//        } catch (OrtException e) {
//            log.error("onnx模型语义相似度计算结果获取异常:{}", e);
//            throw new RuntimeException("onnx模型语义相似度计算结果获取异常");
//        }
//        final float[] floats = result[0][0];
//        List<Float> floatList = new CopyOnWriteArrayList<>();
//        for (int i = 0; i < floats.length; i++) {
//            floatList.add(Float.valueOf(floats[i]));
//        }
//        return floatList;
//    }
//}
