/**
 *
 */
package com.netfinworks.site.web.action.processor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.Validator;

@Component
public class ValidationProcessor {

    @Autowired
    private Validator validator;

    public List<ObjectError> validateForm(Object target, Validator validator) throws BindException {
        DataBinder db = new DataBinder(target);
        db.setValidator(validator);
        db.validate();
        BindingResult br = db.getBindingResult();
        if (br.hasErrors()) {
            return br.getAllErrors();
        }
        return null;
    }

    public void validate(Object target, Validator validator) throws BindException {
        DataBinder db = new DataBinder(target);
        db.setValidator(validator);
        db.validate();
        BindingResult br = db.getBindingResult();
        if (br.hasErrors()) {
            throw new BindException(br);
        }
    }

    public void validate(Object target, Object... groups) throws BindException {
        DataBinder db = new DataBinder(target);
        db.setValidator(validator);
        db.validate(groups);
        BindingResult br = db.getBindingResult();
        if (br.hasErrors()) {
            throw new BindException(br);
        }
    }

    public void validate(Object target, Errors errors, Object... groups) {
        ((SmartValidator) validator).validate(target, errors, groups);
    }
}
