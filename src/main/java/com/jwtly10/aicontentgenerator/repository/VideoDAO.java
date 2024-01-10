package com.jwtly10.aicontentgenerator.repository;

import com.jwtly10.aicontentgenerator.model.VideoData;

import java.util.List;
import java.util.Optional;

public interface VideoDAO<T> {
    List<T> list();

    void create(T t);

    Optional<T> get(String processId);

    int update(T t);

    List<VideoData> getAll(int userId);

    int delete(int id);
}
