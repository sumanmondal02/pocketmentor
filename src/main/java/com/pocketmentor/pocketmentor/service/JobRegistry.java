package com.pocketmentor.pocketmentor.service;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JobRegistry {
    public record Job(String status, String error) {}
    private final Map<Long, Job> jobs = new ConcurrentHashMap<>();

    public synchronized boolean tryStart(Long noteId) {
        Job j = jobs.get(noteId);
        if (j != null && "PROCESSING".equals(j.status())) return false;
        jobs.put(noteId, new Job("PROCESSING", null));
        return true;
    }
    public void done(Long id) { jobs.put(id, new Job("COMPLETED", null)); }
    public void fail(Long id, String msg) { jobs.put(id, new Job("FAILED", msg)); }
    public Job get(Long id) { return jobs.get(id); }
}