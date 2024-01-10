package com.jwtly10.aicontentgenerator.service;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;

@Slf4j
public class BackgroundVideoService {
    public static String getBackgroundVideo(String userChosenVideoType) {
        log.info("Picking a background video for userChosenVideoType: {}", userChosenVideoType);

        List<String> availableMinecraftVideos = List.of(
                "minecraft_parkour_1.mp4",
                "minecraft_parkour_2.mp4"

        );

        if (userChosenVideoType.equals("Minecraft Parkour")) {
            return availableMinecraftVideos.get(new Random().nextInt(availableMinecraftVideos.size()));
        } else {
            return availableMinecraftVideos.get(new Random().nextInt(availableMinecraftVideos.size()));
        }
    }
}
