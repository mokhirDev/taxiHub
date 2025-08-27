package com.mokhir.dev.telegram.bot.taxi.hub.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DateShiftFormatter {

    private static final Set<String> KNOWN_PATTERNS = Set.of(
            "EEE", "EEEE", "d", "dd", "MMM", "MMMM"
    );


    public static String callBackFormat(String pattern, Locale locale) {
        LocalDate date = LocalDate.now();
        // 1. Проверим на сдвиг +N или -N
        int shift = 0;
        String basePattern = pattern;

        if (pattern.matches(".*[+-]\\d+$")) {
            int signIndex = Math.max(pattern.lastIndexOf('+'), pattern.lastIndexOf('-'));
            basePattern = pattern.substring(0, signIndex);

            shift = Integer.parseInt(pattern.substring(signIndex));
        }
        if (basePattern.startsWith("{") && basePattern.endsWith("}")) {
            basePattern = basePattern.substring(1, basePattern.length() - 1);
        }

        LocalDate shiftedDate = date.plusDays(shift);

        // 3. Разбиваем составной паттерн
        String[] parts = basePattern.split("\\.");
        StringBuilder javaPattern = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];

            if (KNOWN_PATTERNS.contains(part)) {
                javaPattern.append(part);
            } else {
                throw new IllegalArgumentException("Неизвестный паттерн: " + part);
            }

            if (i < parts.length - 1) {
                javaPattern.append("."); // сохраняем разделитель
            }
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(javaPattern.toString(), locale);
        return shiftedDate.format(formatter);
    }

    public static String textCodeFormat(String patternWithShift, Locale locale) {
        LocalDate date = LocalDate.now();
        StringBuilder sb = new StringBuilder();
        // 1. Выделяем сдвиг (например "+6" или "-3")
        int shift = 0;
        String pattern = patternWithShift;

        if (patternWithShift.matches(".*[+-]\\d+$")) {
            int signIndex = Math.max(patternWithShift.lastIndexOf('+'), patternWithShift.lastIndexOf('-'));
            pattern = patternWithShift.substring(0, signIndex).trim();
            shift = Integer.parseInt(patternWithShift.substring(signIndex));
        }

        // 2. Убираем фигурные скобки
        if (pattern.startsWith("{") && pattern.endsWith("}")) {
            pattern = pattern.substring(1, pattern.length() - 1);
        }

        // 3. Дата со сдвигом
        LocalDate shiftedDate = date.plusDays(shift);

        // 4. Форматируем по DateTimeFormatter
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, locale);
        return shiftedDate.format(formatter);
    }

    public static String capitalizeWords(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        return Arrays.stream(input.split(" "))
                .map(word -> word.isEmpty()
                        ? word
                        : word.substring(0, 1).toUpperCase() + word.substring(1))
                .collect(Collectors.joining(" "));
    }

}