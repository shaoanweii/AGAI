package com.voc.service.components.onnx.bertTokenizer;

import java.util.List;

public interface Tokenizer {

    public List<String> tokenize(String text);

}
