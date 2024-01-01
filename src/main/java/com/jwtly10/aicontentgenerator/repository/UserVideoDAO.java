package com.jwtly10.aicontentgenerator.repository;

import java.util.List;
import java.util.Optional;

public interface UserVideoDAO<T> {
    List<T> list();

    void create(T t);

    Optional<T> get(int userId);

    Optional<T> get(String videoName);

    int update(T t, int id);

    int delete(int id);

}