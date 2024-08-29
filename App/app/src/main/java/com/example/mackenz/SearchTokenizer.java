package com.example.mackenz;

import java.util.ArrayList;
import java.util.List;

public class SearchTokenizer {
    private String buffer;
    private List<Token> tokens;
    private int position;

    public SearchTokenizer(String text) {
        this.buffer = text;
        this.tokens = new ArrayList<>();
        tokenize();
        this.position = 0;
    }

    private void tokenize() {
        buffer = buffer.trim();
        while (!buffer.isEmpty()) {
            char firstChar = buffer.charAt(0);

            if (Character.isDigit(firstChar)) {
                extractToken(Character::isDigit, Token.Type.NUMBER);
            } else if (Character.isLetter(firstChar)) {
                if (buffer.length() > 1 && Character.isDigit(buffer.charAt(1))) {
                    extractToken(c -> Character.isLetter(c) || Character.isDigit(c), Token.Type.ALPHANUMERIC);
                } else {
                    extractToken(Character::isLetter, Token.Type.WORD);
                }
            } else if (!Character.isWhitespace(firstChar)) {
                extractToken(c -> !Character.isLetterOrDigit(c) && !Character.isWhitespace(c), Token.Type.PUNCTUATION);
            }
            buffer = buffer.trim();
        }
    }

    private void extractToken(java.util.function.Predicate<Character> predicate, Token.Type type) {
        int i = 0;
        while (i < buffer.length() && predicate.test(buffer.charAt(i))) {
            i++;
        }
        tokens.add(new Token(buffer.substring(0, i), type));
        buffer = buffer.substring(i).trim();
    }

    public boolean hasNext() {
        return position < tokens.size();
    }

    public Token next() {
        return tokens.get(position++);
    }
}
