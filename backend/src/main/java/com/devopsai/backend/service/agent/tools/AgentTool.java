package com.devopsai.backend.service.agent.tools;

import java.util.Map;

public interface AgentTool {
    String getName();
    String getDescription();
    Map<String, Object> execute(Map<String, Object> params);
}
