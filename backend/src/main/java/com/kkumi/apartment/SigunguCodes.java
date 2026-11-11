package com.kkumi.apartment;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class SigunguCodes {
    private static final String PATH = "apartment/sigungu.csv";

    private final Map<String, String> names;

    public SigunguCodes() {
        // unmodifiableMap : 읽기 전용 map, 실수로 어디서도 바꾸지 못하게 막음
        this.names = Collections.unmodifiableMap(load());
    }

    public List<String> getAllCodes() {
        return List.copyOf(names.keySet());
    }

    public Optional<String> findName(String code) {
        return Optional.ofNullable(names.get(code));
    }

    private static Map<String, String> load() {
        Map<String, String> map = new LinkedHashMap<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ClassPathResource(PATH).getInputStream(), StandardCharsets.UTF_8))){
            reader.readLine();
            String line;
            while((line = reader.readLine()) != null) {
                if(line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(",", 2);
                map.put(parts[0].trim(), parts[1].trim());
            }
        }
        catch (IOException e) {
            throw new UncheckedIOException("시군구 코드표 읽기 실패: " + PATH, e);
        }
        return map;
    }
}
