package com.example.mackenz;

import java.util.ArrayList;
import java.util.List;

public class SearchParser {
    private SearchTokenizer tokenizer;
    private List<String> parsedQuery;

    public SearchParser(SearchTokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.parsedQuery = new ArrayList<>();
        parse();
    }

    private void parse() {
        while (tokenizer.hasNext()) {
            Token token = tokenizer.next();
            switch (token.getType()) {
                case WORD:
                case NUMBER:
                case ALPHANUMERIC:
                    parsedQuery.add(token.getToken());
                    break;
                case PUNCTUATION:
                    // Handle punctuation based on specific use case (e.g., ignore or special treatment)
                    break;
            }
        }
    }

    public List<String> getParsedQuery() {
        return parsedQuery;
    }
}
