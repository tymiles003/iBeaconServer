package com.aemreunal.controller.project;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import com.aemreunal.exception.project.ProjectNotFoundException;

/*
 ***************************
 * Copyright (c) 2014      *
 *                         *
 * This code belongs to:   *
 *                         *
 * @author Ahmet Emre Ünal *
 * S001974                 *
 *                         *
 * aemreunal@gmail.com     *
 * emre.unal@ozu.edu.tr    *
 *                         *
 * aemreunal.com           *
 ***************************
 */

@ControllerAdvice
public class ProjectControllerAdvice {
    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<JSONObject> projectNotFoundExceptionHandler(ProjectNotFoundException ex) {
        Map<String, String> errorMessage = new HashMap<>(2);
        errorMessage.put("reason", "project");
        errorMessage.put("error", ex.getLocalizedMessage());
        JSONObject responseBody = new JSONObject(errorMessage);
        return new ResponseEntity<JSONObject>(responseBody, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {ConstraintViolationException.class, TransactionSystemException.class })
    public ResponseEntity<JSONObject> constraintViolationExceptionHandler(ConstraintViolationException ex) {
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        Map<String, Object> errorMessage = new HashMap<>(3);
        errorMessage.put("reason", "project");
        errorMessage.put("error", "Constraint violation error ocurred! Unable to save project.");
        errorMessage.put("violations", formatViolations(violations));
        JSONObject responseBody = new JSONObject(errorMessage);
        return new ResponseEntity<JSONObject>(responseBody, HttpStatus.BAD_REQUEST);
    }

    private JSONArray formatViolations(Set<ConstraintViolation<?>> violations) {
        JSONArray violationDescriptions = new JSONArray();
        for (ConstraintViolation<?> violation : violations) {
            Map<String, String> violationDescription = new HashMap<>(2);
            violationDescription.put("property", violation.getPropertyPath().toString());
            violationDescription.put("violation", violation.getMessage());
            violationDescriptions.add(violationDescription);
        }
        return violationDescriptions;
    }
}
