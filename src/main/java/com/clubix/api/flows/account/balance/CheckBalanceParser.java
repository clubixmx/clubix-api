package com.clubix.api.flows.account.balance;

import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CheckBalanceParser {

    private static final Pattern TARGET_10_DIGITS = Pattern.compile("\\b(\\d{10})\\b");

    public Optional<String> parseTarget(String text) {
        String normalized = text == null ? "" : text.trim();
        Matcher m = TARGET_10_DIGITS.matcher(normalized);
        if (m.find()) return Optional.ofNullable(m.group(1));
        return Optional.empty();
    }
}
