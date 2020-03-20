package com.helospark.glslplugin.transition;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL20.glUseProgram;

import java.io.FileInputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL31;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Charsets;
import com.helospark.glslplugin.GlslPlatform;
import com.helospark.glslplugin.util.GlslUtil;
import com.helospark.glslplugin.util.RenderBufferData;
import com.helospark.glslplugin.util.RenderBufferProvider;
import com.helospark.glslplugin.util.UniformUtil;
import com.helospark.glslplugin.util.VertexBufferProvider;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DependentClipProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PointProvider;
import com.helospark.tactview.core.timeline.effect.transition.AbstractVideoTransitionEffect;
import com.helospark.tactview.core.timeline.effect.transition.InternalStatelessVideoTransitionEffectRequest;
import com.helospark.tactview.core.timeline.image.ClipImage;

public class GlTransitionsTransition extends AbstractVideoTransitionEffect {
    protected String vertexShader;
    protected String fragmentShader;

    protected RenderBufferProvider renderBufferProvider;
    protected VertexBufferProvider vertexBufferProvider;
    protected GlslUtil glslUtil;
    private UniformUtil uniformUtil;

    private Map<String, KeyframeableEffect> parameters = new LinkedHashMap<>();

    public GlTransitionsTransition(TimelineInterval interval, RenderBufferProvider renderBufferProvider, VertexBufferProvider vertexBufferProvider, GlslUtil glslUtil,
            UniformUtil uniformUtil, String fragmentShader) {
        super(interval);

        this.renderBufferProvider = renderBufferProvider;
        this.vertexBufferProvider = vertexBufferProvider;
        this.glslUtil = glslUtil;
        this.uniformUtil = uniformUtil;

        this.vertexShader = "shaders/common/common.vs";
        this.fragmentShader = fragmentShader;
    }

    public GlTransitionsTransition(JsonNode node, LoadMetadata loadMetadata, RenderBufferProvider renderBufferProvider, VertexBufferProvider vertexBufferProvider, GlslUtil glslUtil,
            UniformUtil uniformUtil) {
        super(node, loadMetadata);

        this.renderBufferProvider = renderBufferProvider;
        this.vertexBufferProvider = vertexBufferProvider;
        this.glslUtil = glslUtil;
        this.uniformUtil = uniformUtil;

        vertexShader = "shaders/common/common.vs";
        fragmentShader = "gltransitions:shaders/gl-transitions/angular.glsl";
    }

    @Override
    protected ClipImage applyTransitionInternal(InternalStatelessVideoTransitionEffectRequest request) {
        return GlslPlatform.runOnGlThread(() -> {
            glDisable(GL_DEPTH_TEST);

            if (vertexShader == null || fragmentShader == null) {
                return ClipImage.sameSizeAs(request.getFirstFrame());
            }

            int programId = glslUtil.useProgram(vertexShader, fragmentShader);

            int height = request.getFirstFrame().getHeight();
            int width = request.getFirstFrame().getWidth();
            GL11.glViewport(0, 0, width, height);

            RenderBufferData renderbuffer = glslUtil.attachOutputBuffer(height, width);

            int texture1 = glslUtil.bindClipImageToTexture(programId, "fromImage", request.getFirstFrame(), 1);
            int texture2 = glslUtil.bindClipImageToTexture(programId, "toImage", request.getSecondFrame(), 2);

            bindUniforms(programId, request);

            glslUtil.renderFullScreenQuad();

            ClipImage result = glslUtil.readColorAttachement(width, height);

            vertexBufferProvider.unbind();
            glBindTexture(GL_TEXTURE_2D, 0);
            glUseProgram(0);
            GL31.glDeleteTextures(texture1);
            GL31.glDeleteTextures(texture2);
            renderBufferProvider.returnRenderBufferData(width, height, renderbuffer);

            return result;
        });
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        List<ValueProviderDescriptor> valueProviders = super.getValueProvidersInternal();

        for (Map.Entry<String, KeyframeableEffect> entry : parameters.entrySet()) {
            ValueProviderDescriptor providerDescriptor = ValueProviderDescriptor.builder()
                    .withKeyframeableEffect(entry.getValue())
                    .withName(entry.getKey())
                    .build();

            valueProviders.add(providerDescriptor);
        }

        return valueProviders;
    }

    private KeyframeableEffect convertToParameter(String type, String defaultValueString) {
        switch (type) {
            case "float" : {
                double defaultValue = Double.parseDouble(defaultValueString);
                return new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(defaultValue));
            }
            case "int" : {
                int defaultValue = Integer.parseInt(defaultValueString);
                return new DoubleProvider(0, defaultValue * 2, new MultiKeyframeBasedDoubleInterpolator((double) defaultValue));
            }
            case "vec3" : {
                double[] components = parseVec3(defaultValueString);
                return ColorProvider.fromDefaultValue(components[0], components[1], components[2]);
            }
            case "vec4" : {
                double[] components = parseVec4(defaultValueString);
                return ColorProvider.fromDefaultValue(components[0], components[1], components[2]);
            }
            case "ivec2" : {
                double[] components = parseVec2(defaultValueString);
                return PointProvider.of(components[0], components[1]);
            }
            case "vec2" : {
                double[] components = parseVec2(defaultValueString);
                return PointProvider.ofNormalizedImagePosition(components[0], components[1]);
            }
            case "sampler2D" : {
                return new DependentClipProvider(new StepStringInterpolator());
            }
            default :
                throw new RuntimeException("Unknown type");
        }
    }

    private double[] parseVec4(String defaultValueString) {
        Pattern vec4Pattern = Pattern.compile("vec3\\((.*?),\\s*(.*?),\\s*(.*?),\\s*(.*?)\\)");
        double[] components = new double[4];
        Matcher matcher = vec4Pattern.matcher(defaultValueString);
        if (matcher.matches()) {
            components[0] = Double.parseDouble(matcher.group(1));
            components[1] = Double.parseDouble(matcher.group(2));
            components[2] = Double.parseDouble(matcher.group(3));
            components[3] = Double.parseDouble(matcher.group(4));
        } else {
            vec4Pattern = Pattern.compile("vec4\\((.*?)\\)");
            matcher = vec4Pattern.matcher(defaultValueString);
            if (matcher.matches()) {
                components[0] = Double.parseDouble(matcher.group(1));
                components[1] = Double.parseDouble(matcher.group(1));
                components[2] = Double.parseDouble(matcher.group(1));
                components[4] = Double.parseDouble(matcher.group(1));
            }
        }
        return components;
    }

    private double[] parseVec2(String defaultValueString) {
        defaultValueString = defaultValueString.replaceAll("ivec2", "vec2");

        Pattern vec2Pattern = Pattern.compile("vec2\\((.*?),\\s*(.*?)\\)");
        double[] components = new double[2];
        Matcher matcher = vec2Pattern.matcher(defaultValueString);
        if (matcher.matches()) {
            components[0] = Double.parseDouble(matcher.group(1));
            components[1] = Double.parseDouble(matcher.group(2));
        } else {
            vec2Pattern = Pattern.compile("vec2\\((.*?)\\)");
            matcher = vec2Pattern.matcher(defaultValueString);
            if (matcher.matches()) {
                components[0] = Double.parseDouble(matcher.group(1));
                components[1] = Double.parseDouble(matcher.group(1));
            }
        }
        return components;
    }

    protected double[] parseVec3(String defaultValueString) {
        Pattern vec3Pattern = Pattern.compile("vec3\\((.*?),\\s*(.*?),\\s*(.*?)\\)");
        double[] colorComponents = new double[3];
        Matcher matcher = vec3Pattern.matcher(defaultValueString);
        if (matcher.matches()) {
            colorComponents[0] = Double.parseDouble(matcher.group(1));
            colorComponents[1] = Double.parseDouble(matcher.group(2));
            colorComponents[2] = Double.parseDouble(matcher.group(3));
        } else {
            vec3Pattern = Pattern.compile("vec3\\((.*?)\\)");
            matcher = vec3Pattern.matcher(defaultValueString);
            if (matcher.matches()) {
                colorComponents[0] = Double.parseDouble(matcher.group(1));
                colorComponents[1] = Double.parseDouble(matcher.group(1));
                colorComponents[2] = Double.parseDouble(matcher.group(1));
            }
        }
        return colorComponents;
    }

    protected String readShader() {
        String fileToLoad = this.getClass().getResource("/" + fragmentShader.replaceAll("gltransitions:", "")).getFile();
        try (var inputStream = new FileInputStream(fileToLoad)) {
            byte[] dataBytes = inputStream.readAllBytes();
            return new String(dataBytes, Charsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void initializeValueProviderInternal() {
        super.initializeValueProviderInternal();

        Pattern uniformPatternVariant1 = Pattern.compile("uniform\\s+(.*?)\\s+(.*?);\\s*\\/\\/\\s*=\\s*(.*?)");
        Pattern uniformPatternVariant2 = Pattern.compile("uniform\\s+(.*?)\\s+(.*?);\\s*\\/\\/\\s*=\\s*(.*?)\\s*;.*");
        Pattern uniformPatternVariant3 = Pattern.compile("uniform\\s+(.*?)\\s+(.*?)\\s*\\/\\*\\s*=\\s*(.*?)\\s*\\*\\/;?.*");
        Pattern uniformPatternVariant4 = Pattern.compile("uniform\\s+sampler2D\\s+(.*?)\\s*;.*");

        List<Pattern> uniformPatterns = List.of(uniformPatternVariant1, uniformPatternVariant2, uniformPatternVariant3, uniformPatternVariant4);

        String shader = readShader();

        String[] shaderLines = shader.split("\n");
        for (String line : shaderLines) {
            for (Pattern pattern : uniformPatterns) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    String type = matcher.group(1);
                    String name = matcher.group(2);
                    String defaultValue = null;
                    if (matcher.groupCount() > 2) {
                        defaultValue = matcher.group(3);
                    }
                    List<String> defaultUniforms = List.of("fromImage", "toImage", "progress");
                    if (defaultUniforms.contains(name)) {
                        break;
                    }

                    parameters.put(name, convertToParameter(type, defaultValue));

                }
            }
        }
    }

    private void bindUniforms(int programId, InternalStatelessVideoTransitionEffectRequest request) {
        uniformUtil.bindFloatToUniform(programId, (float) request.getProgress(), "progress");

        TimelinePosition position = request.getEffectPosition();

        for (Map.Entry<String, KeyframeableEffect> entry : parameters.entrySet()) {
            KeyframeableEffect parameterProvider = entry.getValue();
            String name = entry.getKey();
            Class<? extends KeyframeableEffect> providerType = parameterProvider.getClass();

            if (providerType.equals(DoubleProvider.class)) {
                uniformUtil.bindDoubleProviderToUniform(programId, (DoubleProvider) parameterProvider, position, name);
            } else if (providerType.equals(IntegerProvider.class)) {
                uniformUtil.bindIntegerProviderToUniform(programId, (IntegerProvider) parameterProvider, position, name);
            } else if (providerType.equals(ColorProvider.class)) {
                uniformUtil.bindColorProviderToUniform(programId, (ColorProvider) parameterProvider, position, name);
            } else if (providerType.equals(PointProvider.class)) {
                uniformUtil.bindPointProviderToUniform(programId, (PointProvider) parameterProvider, position, name);
            } else if (providerType.equals(PointProvider.class)) {
                uniformUtil.bindPointProviderToUniform(programId, (PointProvider) parameterProvider, position, name);
            } else if (providerType.equals(DependentClipProvider.class)) {
                // TODO: later
                //                String clipId = ((DependentClipProvider)parameterProvider).getValueAt(position);
                //                uniformUtil.bindDependentClipProviderToUniform(programId, (DependentClipProvider) parameterProvider, position, name);
            }
        }
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        // TODO Auto-generated method stub
        return null;
    }

}