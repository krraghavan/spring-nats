package com.github.krr.nats.config;

import com.github.krr.nats.annotations.RuntimeDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

public class ClassExistsCondition implements Condition {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClassExistsCondition.class);

  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    String runtimeAnnotationClassName = RuntimeDependency.class.getName();
    if (metadata.isAnnotated(runtimeAnnotationClassName)) {
      Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(runtimeAnnotationClassName, false);
      if (annotationAttributes != null) {
        LOGGER.trace("Annotation attributes {}", annotationAttributes);
        String[] classesToCheck = (String[]) annotationAttributes.get("classes");
        for (String clazz : classesToCheck) {
          try {
            Class.forName(clazz);
          } catch (Exception e) {
            // some runtime dependency is missing.
            LOGGER.debug("Class {} not found - returning false", clazz);
            return false;
          }
        }
        return true;
      }
    }
    throw new IllegalArgumentException("ClassExistsCondition annotation must contain the RuntimeDependency annotation to " +
        "check for dependent classes.");
  }
}
