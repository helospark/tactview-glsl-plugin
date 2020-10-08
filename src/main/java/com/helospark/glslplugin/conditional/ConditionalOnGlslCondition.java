package com.helospark.glslplugin.conditional;

import java.util.Optional;

import com.helospark.lightdi.conditional.condition.ConditionalEvaluationRequest;
import com.helospark.lightdi.conditional.condition.DependencyCondition;

public class ConditionalOnGlslCondition implements DependencyCondition {
    private Optional<Integer> glslActualMajorVersion;
    private Optional<Integer> glslActualMinorVersion;

    private Optional<GlslVersion> expectedVersion;

    public ConditionalOnGlslCondition(Optional<Integer> glslActualMajorVersion, Optional<Integer> glslActualMinorVersion,
            Optional<GlslVersion> expectedVersion) {
        this.glslActualMajorVersion = glslActualMajorVersion;
        this.glslActualMinorVersion = glslActualMinorVersion;
        this.expectedVersion = expectedVersion;
    }

    @Override
    public boolean evaluate(ConditionalEvaluationRequest request) {
        if (!expectedVersion.isPresent()) {
            return isGlslAvailable();
        } else {
            boolean majorVersionMatch = glslActualMajorVersion.map(actualVersion -> actualVersion >= expectedVersion.get().majorVersion).orElse(false);
            boolean minorVersionMatch = glslActualMinorVersion.map(actualVersion -> actualVersion >= expectedVersion.get().minorVersion).orElse(false);
            return majorVersionMatch && minorVersionMatch;
        }
    }

    protected boolean isGlslAvailable() {
        return glslActualMajorVersion.isPresent();
    }

}
