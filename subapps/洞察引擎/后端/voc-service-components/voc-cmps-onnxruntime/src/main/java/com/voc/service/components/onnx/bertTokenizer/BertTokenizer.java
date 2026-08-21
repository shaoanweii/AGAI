package com.voc.service.components.onnx.bertTokenizer;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import cn.hutool.core.util.StrUtil;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.components.minio.config.MinioConfig;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.errors.*;
import lombok.Cleanup;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Bert Tokenizer
 *
 * @author jadepeng
 */
@Log4j2
public class BertTokenizer implements Tokenizer {


    private String vocabFile = "vocab.txt";
    private InputStream vocabInputStream;
    private Map<String, Integer> tokenIdMap;
    private Map<Integer, String> idTokenMap;
    private final boolean doLowerCase = true;
    private final boolean doBasicTokenize = true;
    private final List<String> neverSplit = new ArrayList<String>();
    private final String unkToken = "[UNK]";
    private final String sepToken = "[SEP]";
    private final String padToken = "[PAD]";
    private final String clsToken = "[CLS]";
    private final String maskToken = "[MASK]";
    private final boolean tokenizeChineseChars = true;
    private BasicTokenizer basicTokenizer;
    private WordpieceTokenizer wordpieceTokenizer;

    private static BertTokenizer bertTokenizer;

    private static final int MAX_LEN = 2048;

    public static BertTokenizer getInstance() {
      /*  if (bertTokenizer == null) {
            synchronized (BertTokenizer.class) {
                if (bertTokenizer == null) {
                    MinioClient minioClient = ServiceContextHolder.getApplicationContext().getBean(MinioClient.class);
                    MinioConfig config = ServiceContextHolder.getApplicationContext().getBean(MinioConfig.class);
                    if (ObjectUtils.isEmpty(minioClient)) {
                        throw new RuntimeException("获取minioClient异常");
                    }
                    if (ObjectUtils.isEmpty(config)) {
                        throw new RuntimeException("获取minioConfig异常");
                    }
                    try {
                        @Cleanup
                        GetObjectResponse object = minioClient.getObject(
                                GetObjectArgs.builder()
                                        .bucket(config.getBucketName())
                                        .object("ai-workflow/vocab.txt")
                                        .build());
                        bertTokenizer = new BertTokenizer(object);
                    } catch (ErrorResponseException e) {
                        throw new RuntimeException(e);
                    } catch (InsufficientDataException e) {
                        throw new RuntimeException(e);
                    } catch (InternalException e) {
                        throw new RuntimeException(e);
                    } catch (InvalidKeyException e) {
                        throw new RuntimeException(e);
                    } catch (InvalidResponseException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    } catch (ServerException e) {
                        throw new RuntimeException(e);
                    } catch (XmlParserException e) {
                        throw new RuntimeException(e);
                    }
//                    URL vocabUrl;
////                        vocabUrl = BertTokenizer.class.getClassLoader().getResources("vocab.txt").nextElement();
//                    List<URL> resources = ResourceUtil.getResources("vocab.txt");
//                    if(ObjectUtils.isEmpty(resources)){
//                        throw new RuntimeException("获取vocab.txt文件异常");
//                    }
//                    vocabUrl = resources.get(0);


                }
            }
        }*/
        return bertTokenizer;
    }

    private BertTokenizer(String vocabFile) {
        this.vocabFile = vocabFile;
        init();
    }

    private BertTokenizer(InputStream inputStream) {
        init(inputStream);
    }

    private BertTokenizer() {
        init();
    }

    private void init(InputStream inputStream) {
        try {
            this.tokenIdMap = loadVocabByInputStream(inputStream);
        } catch (IOException e) {
            log.error("Unable to load vocab due to: ", e);
        }

        this.idTokenMap = new HashMap<>(this.tokenIdMap.size());
        for (String key : tokenIdMap.keySet()) {
            this.idTokenMap.put(tokenIdMap.get(key), key);
        }

        if (doBasicTokenize) {
            this.basicTokenizer = new BasicTokenizer(doLowerCase, neverSplit, tokenizeChineseChars);
        }
        this.wordpieceTokenizer = new WordpieceTokenizer(tokenIdMap, unkToken);
    }

    private void init() {
        if (StrUtil.isNotBlank(vocabFile)) {
            try {
                this.tokenIdMap = loadVocab(vocabFile);
            } catch (IOException e) {
                log.error("Unable to load vocab due to: ", e);
            }
        } else {
            try {
                this.tokenIdMap = loadVocabByInputStream(vocabInputStream);
            } catch (IOException e) {
                log.error("Unable to load vocab due to: ", e);
            }
        }

        this.idTokenMap = new HashMap<>(this.tokenIdMap.size());
        for (String key : tokenIdMap.keySet()) {
            this.idTokenMap.put(tokenIdMap.get(key), key);
        }

        if (doBasicTokenize) {
            this.basicTokenizer = new BasicTokenizer(doLowerCase, neverSplit, tokenizeChineseChars);
        }
        this.wordpieceTokenizer = new WordpieceTokenizer(tokenIdMap, unkToken);
    }

    private Map<String, Integer> loadVocab(String vocabFileName) throws IOException {
        return TokenizerUtils.generateTokenIdMap(new FileInputStream(vocabFileName));
    }

    private Map<String, Integer> loadVocabByInputStream(InputStream inputStream) throws IOException {
        return TokenizerUtils.generateTokenIdMap(inputStream);
    }

    /**
     * Tokenizes a piece of text into its word pieces.
     * <p>
     * This uses a greedy longest-match-first algorithm to perform tokenization
     * using the given vocabulary.
     * <p>
     * For example: input = "unaffable" output = ["un", "##aff", "##able"]
     * <p>
     * Args: text: A single token or whitespace separated tokens. This should have
     * already been passed through `BasicTokenizer`.
     * <p>
     * Returns: A list of wordpiece tokens.
     */
    @Override
    public List<String> tokenize(String text) {
        List<String> splitTokens = new ArrayList<>();
        if (doBasicTokenize) {
            for (String token : basicTokenizer.tokenize(text)) {
                splitTokens.addAll(wordpieceTokenizer.tokenize(token));
            }
        } else {
            splitTokens = wordpieceTokenizer.tokenize(text);
        }
        return splitTokens;
    }

    public String convertTokensToString(List<String> tokens) {
        // Converts a sequence of tokens (string) in a single string.
        return tokens.stream().map(s -> s.replace("##", "")).collect(Collectors.joining(" "));
    }

    public long[][] convertTokensToIds(List<String> tokens) {
        long[][] result = new long[1][];
        int i = 1;
        for (String s : tokens) {
            result[0][i++] = tokenIdMap.get(s);
        }
        result[0][i++] = tokenIdMap.get(sepToken);
        return result;
    }

    static long[] paddingZero(long[] array1, int paddingSize) {
        long[] result = new long[array1.length + paddingSize];
        System.arraycopy(array1, 0, result, 0, array1.length);
        for (int i = array1.length; i < result.length; i++) {
            result[i] = 0;
        }
        return result;
    }

    public Map<String, OnnxTensor> tokenizeOnnxTensor(List<String> texts)
            throws OrtException {
        var env = OrtEnvironment.getEnvironment();
        long[][] textTokensIds = new long[texts.size()][];
        long[][] typeIds = new long[texts.size()][];
        long[][] attention_mask = new long[texts.size()][];
        int rowIndex = 0;
        int maxColumn = 0;
        AtomicInteger atomicInteger = new AtomicInteger(0);
        texts.stream().forEach(e -> {
            List<String> tokens = tokenize(e);
            atomicInteger.set(Math.max(atomicInteger.get(), tokens.size() + 2));
        });
        for (String text : texts) {
            List<String> tokens = tokenize(text);
            long[] tokenIds = new long[atomicInteger.get()];
            int index = 0;
            tokenIds[index++] = tokenIdMap.get(clsToken);
            for (String s : tokens) {
                tokenIds[index++] = tokenIdMap.get(s);
            }
            tokenIds[index++] = tokenIdMap.get(sepToken);
            textTokensIds[rowIndex] = tokenIds;
            typeIds[rowIndex] = buildTokenTypeArray(index < atomicInteger.get() ? atomicInteger.get() : index);
            attention_mask[rowIndex++] = buildAttentionMaskArray(tokenIds);
            maxColumn = Math.max(maxColumn, index);
        }

        // padding 0
        for (int row = 0; row < texts.size(); row++) {
            if (textTokensIds[row].length < maxColumn) {
                // padding 0
                textTokensIds[row] = paddingZero(textTokensIds[row], maxColumn - textTokensIds[row].length);
                typeIds[row] = paddingZero(typeIds[row], maxColumn - typeIds[row].length);
            }
        }

        OnnxTensor inputIds = OnnxTensor.createTensor(env, textTokensIds);
        OnnxTensor tokenTypeIds = OnnxTensor.createTensor(env, typeIds);
        OnnxTensor attentionMask = OnnxTensor.createTensor(env, attention_mask);
        Map<String, OnnxTensor> inputMap = new HashMap<>();
        inputMap.put("input_ids", inputIds);
        inputMap.put("token_type_ids", tokenTypeIds);
        inputMap.put("attention_mask", attentionMask);

        return inputMap;
    }

    long[] buildTokenTypeArray(int size) {
        long[] typeIds = new long[size];
        for (int i = 0; i < size; i++) {
            typeIds[i] = 0;
        }
        return typeIds;
    }

    long[] buildAttentionMaskArray(long[] tokenIds) {
        long[] attentionMask = new long[tokenIds.length];
        for (int i = 0; i < tokenIds.length; i++) {
            if (tokenIds[i] != 0) {
                attentionMask[i] = 1;
            }
        }
        return attentionMask;
    }

    public int vocabSize() {
        return tokenIdMap.size();
    }
}
