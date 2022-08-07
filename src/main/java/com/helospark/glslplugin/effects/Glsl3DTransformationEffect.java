package com.helospark.glslplugin.effects;

import java.util.List;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.glslplugin.util.GlslUtil;
import com.helospark.glslplugin.util.RenderBufferProvider;
import com.helospark.glslplugin.util.UniformUtil;
import com.helospark.glslplugin.util.VertexBufferProvider;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.bezier.BezierDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BooleanProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.util.ReflectionUtil;

public class Glsl3DTransformationEffect extends AbstractRegularGlslStatelessVideoEffect {
    private UniformUtil uniformUtil;

    private DoubleProvider xTranslate;
    private DoubleProvider yTranslate;
    private DoubleProvider zTranslate;

    private DoubleProvider xRotation;
    private DoubleProvider yRotation;
    private DoubleProvider zRotation;

    private DoubleProvider xScale;
    private DoubleProvider yScale;
    private DoubleProvider zScale;

    private DoubleProvider fieldOfView;

    private BooleanProvider specularHighlightEnabled;
    private DoubleProvider specularShininessProvider;
    private DoubleProvider specularLightStrengthProvider;

    public Glsl3DTransformationEffect(TimelineInterval interval, GlslUtil glslUtil, RenderBufferProvider renderBufferProvider, VertexBufferProvider vertexBufferProvider,
            UniformUtil uniformUtil) {
        super(interval, renderBufferProvider, vertexBufferProvider, glslUtil);

        this.uniformUtil = uniformUtil;

        this.vertexShader = "shaders/3dtransform/vertexshader.vs";
        this.fragmentShader = "shaders/3dtransform/fragmentshader.fs";
    }

    public Glsl3DTransformationEffect(JsonNode node, LoadMetadata loadMetadata, GlslUtil glslUtil, RenderBufferProvider renderBufferProvider,
            VertexBufferProvider vertexBufferProvider, UniformUtil uniformUtil) {
        super(node, loadMetadata, glslUtil, renderBufferProvider, vertexBufferProvider);
        this.uniformUtil = uniformUtil;
    }

    public Glsl3DTransformationEffect(Glsl3DTransformationEffect effect, CloneRequestMetadata cloneRequestMetadata) {
        super(effect, cloneRequestMetadata);

        ReflectionUtil.copyOrCloneFieldFromTo(effect, this, cloneRequestMetadata);
    }

    @Override
    protected void initializeValueProviderInternal() {
        xTranslate = new DoubleProvider(-100, 100, new BezierDoubleInterpolator(0.0));
        yTranslate = new DoubleProvider(-100, 100, new BezierDoubleInterpolator(0.0));
        zTranslate = new DoubleProvider(-100, 100, new BezierDoubleInterpolator(7.0));

        xRotation = new DoubleProvider(-10, 10, new BezierDoubleInterpolator(0.0));
        yRotation = new DoubleProvider(-10, 10, new BezierDoubleInterpolator(0.0));
        zRotation = new DoubleProvider(-10, 10, new BezierDoubleInterpolator(0.0));

        xScale = new DoubleProvider(0, 10, new BezierDoubleInterpolator(1.0));
        yScale = new DoubleProvider(0, 10, new BezierDoubleInterpolator(1.0));
        zScale = new DoubleProvider(0, 10, new BezierDoubleInterpolator(1.0));

        fieldOfView = new DoubleProvider(10, 120, new BezierDoubleInterpolator(45.0));

        specularHighlightEnabled = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(0.0));
        specularShininessProvider = new DoubleProvider(1.0, 150.0, new BezierDoubleInterpolator(20.0));
        specularLightStrengthProvider = new DoubleProvider(0.0, 1.0, new BezierDoubleInterpolator(0.8));
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        ValueProviderDescriptor xTranslateProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(xTranslate)
                .withName("x translate")
                .withGroup("translate")
                .build();
        ValueProviderDescriptor yTranslateProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(yTranslate)
                .withName("y translate")
                .withGroup("translate")
                .build();
        ValueProviderDescriptor zTranslateProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(zTranslate)
                .withName("z translate")
                .withGroup("translate")
                .build();

        ValueProviderDescriptor xRotationProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(xRotation)
                .withName("x rotation")
                .withGroup("rotation")
                .build();
        ValueProviderDescriptor yRotationProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(yRotation)
                .withName("y rotation")
                .withGroup("rotation")
                .build();
        ValueProviderDescriptor zRotationProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(zRotation)
                .withName("z rotation")
                .withGroup("rotation")
                .build();

        ValueProviderDescriptor xScaleProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(xScale)
                .withName("x scale")
                .withGroup("scale")
                .build();
        ValueProviderDescriptor yScaleProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(yScale)
                .withName("y scale")
                .withGroup("scale")
                .build();
        ValueProviderDescriptor zScaleProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(zScale)
                .withName("z scale")
                .withGroup("scale")
                .build();

        ValueProviderDescriptor fieldOfViewProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(fieldOfView)
                .withName("FOV")
                .withGroup("camera")
                .build();

        ValueProviderDescriptor specularHighlightProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(specularHighlightEnabled)
                .withName("Specular highlight")
                .withGroup("lighting")
                .build();

        ValueProviderDescriptor specularShininessProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(specularShininessProvider)
                .withName("Specular shininess")
                .withGroup("lighting")
                .withShowPredicate(time -> specularHighlightEnabled.getValueWithoutScriptAt(time))
                .build();
        ValueProviderDescriptor specularLightStrengthProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(specularLightStrengthProvider)
                .withName("Specular strength")
                .withGroup("lighting")
                .withShowPredicate(time -> specularHighlightEnabled.getValueWithoutScriptAt(time))
                .build();

        return List.of(xTranslateProviderDescriptor, yTranslateProviderDescriptor, zTranslateProviderDescriptor,
                xRotationProviderDescriptor, yRotationProviderDescriptor, zRotationProviderDescriptor,
                xScaleProviderDescriptor, yScaleProviderDescriptor, zScaleProviderDescriptor, fieldOfViewProviderDescriptor,
                specularHighlightProviderDescriptor, specularShininessProviderDescriptor, specularLightStrengthProviderDescriptor);
    }

    @Override
    protected void bindUniforms(int programId, StatelessEffectRequest request) {
        float aspectRatio = (float) request.getCanvasWidth() / request.getCanvasHeight();
        Matrix4f viewProjectionMatrix = new Matrix4f()
                .perspective((float) Math.toRadians(fieldOfView.getValueAt(request.getEffectPosition(), request.getEvaluationContext()).floatValue()), aspectRatio, 0.01f, 100.0f)
                .lookAt(0.0f, 0.0f, 10.0f,
                        0.0f, 0.0f, 0.0f,
                        0.0f, 1.0f, 0.0f);
        TimelinePosition position = request.getEffectPosition();
        Matrix4f modelMatrix = new Matrix4f()
                .translate(xTranslate.getValueAt(position, request.getEvaluationContext()).floatValue(), yTranslate.getValueAt(position, request.getEvaluationContext()).floatValue(),
                        zTranslate.getValueAt(position, request.getEvaluationContext()).floatValue())
                .rotate(xRotation.getValueAt(position, request.getEvaluationContext()).floatValue(), 1.0f, 0.0f, 0.0f)
                .rotate(yRotation.getValueAt(position, request.getEvaluationContext()).floatValue(), 0.0f, 1.0f, 0.0f)
                .rotate(zRotation.getValueAt(position, request.getEvaluationContext()).floatValue(), 0.0f, 0.0f, 1.0f)
                .scale(aspectRatio, 1.0f, 1.0f)
                .scale(xScale.getValueAt(position, request.getEvaluationContext()).floatValue(), yScale.getValueAt(position, request.getEvaluationContext()).floatValue(),
                        zScale.getValueAt(position, request.getEvaluationContext()).floatValue());

        Matrix4f mvpMatrix = viewProjectionMatrix.mul(modelMatrix);

        uniformUtil.bind4x4Matrix(programId, "modelViewProjectionMatrix", mvpMatrix);
        uniformUtil.bind4x4Matrix(programId, "modelMatrix", modelMatrix);

        Matrix3f normalMatrix = new Matrix3f();
        mvpMatrix.get3x3(normalMatrix);
        normalMatrix = normalMatrix.invert().transpose();

        uniformUtil.bind3x3Matrix(programId, "normalMatrix", normalMatrix);

        uniformUtil.bindBooleanProviderToUniform(programId, specularHighlightEnabled, position, "specularEnabled", request.getEvaluationContext());
        uniformUtil.bindDoubleProviderToUniform(programId, specularShininessProvider, position, "specularShininess", request.getEvaluationContext());
        uniformUtil.bindDoubleProviderToUniform(programId, specularLightStrengthProvider, position, "specularLightStrength", request.getEvaluationContext());
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new Glsl3DTransformationEffect(this, cloneRequestMetadata);
    }

}
