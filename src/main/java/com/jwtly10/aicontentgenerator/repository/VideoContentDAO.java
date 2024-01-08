package com.jwtly10.aicontentgenerator.repository;

import java.util.List;
import java.util.Optional;

public interface VideoContentDAO<T> {
    List<T> list();

    void create(T t);

    Optional<T> get(String videoId);

    int update(T t, String videoId);

    int delete(String videoId);
}
