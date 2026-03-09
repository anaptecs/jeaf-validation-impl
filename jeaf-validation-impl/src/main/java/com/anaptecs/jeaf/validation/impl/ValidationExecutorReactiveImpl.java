package com.anaptecs.jeaf.validation.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import com.anaptecs.jeaf.tools.api.validation.ValidationTools;
import com.anaptecs.jeaf.validation.api.ValidationExecutorReactive;
import reactor.core.publisher.Mono;

public class ValidationExecutorReactiveImpl implements ValidationExecutorReactive {

  @Override
  public Mono<Void> validateRequest(Class<?> pService, Object... pRequestParameters) {
    return Mono.create(v -> this.validateRequestInternal(pService, pRequestParameters));

  }

  private void validateRequestInternal(Class<?> pService, Object... pRequestParameters) {
    // Validate all parameters
    Set<ConstraintViolation<Object>> lConstraintViolations = new HashSet<>();
    for (Object lNext : pRequestParameters) {
      lConstraintViolations.addAll(this.validateRequestParameter(lNext));
    }
    if (lConstraintViolations.isEmpty() == false) {
      throw new ConstraintViolationException(lConstraintViolations);
    }
  }

  private Set<ConstraintViolation<Object>> validateRequestParameter(Object pRequestParamater) {
    // Check if parameter is array of collection. In this case each object has to be check individually
    Set<ConstraintViolation<Object>> lConstraintViolations;

    // Validate all objects inside an array. Arrays of primitive types are ignored.
    Class<? extends Object> lClass = pRequestParamater.getClass();
    if (lClass.isArray() && lClass.getComponentType().isPrimitive() == false) {
      lConstraintViolations = new HashSet<>();
      Object[] pObjectArray = (Object[]) pRequestParamater;
      for (Object lNext : pObjectArray) {
        lConstraintViolations.addAll(this.validateRequestParameter(lNext));
      }
    }
    // Validate all objects inside a collection.
    else if (pRequestParamater instanceof Collection<?>) {
      lConstraintViolations = new HashSet<>();
      Collection<?> lCollection = (Collection<?>) pRequestParamater;
      for (Object lNext : lCollection) {
        lConstraintViolations.addAll(this.validateRequestParameter(lNext));
      }
    }
    // Validate plain object
    else {
      lConstraintViolations = ValidationTools.getValidationTools().validateObject(pRequestParamater);
    }
    return lConstraintViolations;
  }

  @Override
  public Mono<Void> validateResponse(Class<?> pService, Object pResponseObject) {
    return Mono.create(v -> ValidationTools.getValidationTools().enforceObjectValidation(pResponseObject));
  }
}
