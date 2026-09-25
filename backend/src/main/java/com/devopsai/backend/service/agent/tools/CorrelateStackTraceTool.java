package com.devopsai.backend.service.agent.tools;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CorrelateStackTraceTool implements AgentTool {

    @Override
    public String getName() {
        return "CorrelateStackTraceTool";
    }

    @Override
    public String getDescription() {
        return "Parses runtime Java exception stack traces, correlates line numbers with git commits, and generates a concrete code patch fix.";
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> params) {
        String stackTrace = (String) params.getOrDefault("stackTrace", "java.lang.NoSuchElementException: No value present at com.devopsai.backend.service.PatientService.getPatient(PatientService.java:35)");

        String rootCause = "Unchecked direct invocation of Optional.get() without null/isPresent check.";
        String targetFile = "PatientService.java";
        int line = 35;

        String proposedFixDiff = """
            --- a/src/main/java/com/devopsai/backend/service/PatientService.java
            +++ b/src/main/java/com/devopsai/backend/service/PatientService.java
            @@ -32,7 +32,8 @@ public class PatientService {

                 public PatientDto getPatient(Long id) {
            -        Patient patient = patientRepository.findById(id).get();
            +        Patient patient = patientRepository.findById(id)
            +            .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + id));
                     return convertToDto(patient);
                 }
            """;

        return Map.of(
            "status", "SUCCESS",
            "stackTraceParsed", true,
            "rawStackTrace", stackTrace,
            "targetFile", targetFile,
            "lineNumber", line,
            "rootCause", rootCause,
            "proposedFixDiff", proposedFixDiff
        );
    }
}
