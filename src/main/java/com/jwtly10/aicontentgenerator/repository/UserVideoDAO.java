package com.jwtly10.aicontentgenerator.repository;

import java.util.List;
import java.util.Optional;

public interface UserVideoDAO<T> {
    List<T> list();

    long create(T t);

    Optional<T> get(String processId, int userId);

    int update(T t, String processId);

    int delete(int id);

    List<T> getPending(int limit);

}
