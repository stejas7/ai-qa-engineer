package com.aiqa.agent;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Mission-scoped context shared safely across specialized Auravis agents. @author Tejas Shah */
public final class AgentContext {
    private final UUID missionId;
    private final String company;
    private final String targetUrl;
    private final Map<String, Object> state = new LinkedHashMap<>();

    public AgentContext(UUID missionId, String company, String targetUrl) {
        this.missionId = missionId;
        this.company = company == null || company.isBlank() ? "default" : company;
        this.targetUrl = targetUrl;
    }

    public UUID missionId() { return missionId; }
    public String company() { return company; }
    public String targetUrl() { return targetUrl; }
    public void put(String key, Object value) { state.put(key, value); }
    public Object get(String key) { return state.get(key); }
    public Map<String, Object> snapshot() { return Collections.unmodifiableMap(new LinkedHashMap<>(state)); }
}
