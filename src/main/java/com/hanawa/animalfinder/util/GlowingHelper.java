package com.hanawa.animalfinder.util;

import net.minecraft.world.entity.Entity;

import java.util.*;

public class GlowingHelper {

    private Map<UUID, TimerTask> taskList;
    private final Timer timer;
    private static GlowingHelper INSTANCE;

    private GlowingHelper() {
        timer = new Timer();
        taskList = new HashMap<UUID, TimerTask>();
    }

    public static GlowingHelper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GlowingHelper();
        }

        return INSTANCE;
    }

    public void triggerGlowing(Entity animal, int durationSeconds) {
        UUID animalId = animal.getUUID();
        TimerTask existingTask = taskList.get(animalId);

        if (existingTask != null) {
            existingTask.cancel();
        }

        animal.setGlowingTag(true);

        TimerTask task = new TimerTask() {
            public void run() {
                animal.setGlowingTag(false);
                taskList.remove(animalId);
            }
        };

        taskList.put(animalId, task);
        timer.schedule(task, durationSeconds);
    }
}
