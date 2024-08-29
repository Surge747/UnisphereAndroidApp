package com.example.mackenz;

public class Token {
    public enum Type {
        WORD,    // 英文单词
        NUMBER,  // 数字
        ALPHANUMERIC, // 英文单词和数字的组合
        PUNCTUATION  // 标点符号
    }

    private final String token;
    private final Type type;

    public Token(String token, Type type) {
        this.token = token;
        this.type = type;
    }

    public String getToken() {
        return token;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return type + "(" + token + ")";
    }
}
