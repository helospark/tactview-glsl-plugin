package com.helospark.glslplugin.conditional;

import java.lang.annotation.Annotation;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helospark.glslplugin.init.GlslInitializer;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.conditional.AnnotationBasedConditionalProcessorFactory;
import com.helospark.lightdi.conditional.condition.DependencyCondition;
import com.helospark.lightdi.util.LightDiAnnotation;

@Component
public class ConditionalOnGLSLConditionFactory implements AnnotationBasedConditionalProcessorFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConditionalOnGLSLConditionFactory.class);

    @Override
    public Class<? extends Annotation> getAnnotation() {
        return ConditionalOnGlsl.class;
    }

    @Override
    public DependencyCondition getDependencyCondition(LightDiAnnotation annotation) {
        Optional<String> glslVersion = GlslInitializer.getGlslVersion();

        Optional<Integer> glslActualMajorVersion = getGroupFromVersionString(glslVersion, 0);
        Optional<Integer> glslActualMinorVersion = getGroupFromVersionString(glslVersion, 1);

        Optional<GlslVersion> expectedVersion = Optional.ofNullable(annotation.getAttributeAs("version", GlslVersion.class)).filter(a -> a != GlslVersion.NULL);

        return new ConditionalOnGlslCondition(glslActualMajorVersion, glslActualMinorVersion, expectedVersion);
    }

    protected Optional<Integer> getAnnotationGlslAttribute(LightDiAnnotation annotation, String attribute) {
        return Optional.ofNullable(annotation.getAttributeAs(attribute, Integer.class)).filter(a -> a != -1);
    }

    protected Optional<Integer> getGroupFromVersionString(Optional<String> glslVersion, int group) {
        return glslVersion.filter(a -> a.matches("\\d+\\.\\d+"))
                .map(a -> a.split("\\.")[group])
                .map(a -> Integer.valueOf(a));
    }

}
