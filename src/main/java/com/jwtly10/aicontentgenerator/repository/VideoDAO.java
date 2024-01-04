package com.jwtly10.aicontentgenerator.repository;

import java.util.List;
import java.util.Optional;

public interface VideoDAO<T> {
    List<T> list();

    void create(T t);

    Optional<T> get(String processId);

    int update(T t);

    int delete(int id);
}
