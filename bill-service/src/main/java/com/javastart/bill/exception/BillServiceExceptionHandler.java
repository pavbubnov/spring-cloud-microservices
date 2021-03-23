package com.javastart.bill.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;


@ControllerAdvice
public class BillServiceExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatus status,
                                                                  WebRequest request) {
        BindingResult result = ex.getBindingResult();
        List<String> errors = result.getFieldErrors().stream().map(objectError ->
                ((FieldError) objectError).getDefaultMessage()).collect(Collectors.toList());

        List<String> fields = result.getFieldErrors().stream().map(objectError ->
                ((FieldError) objectError).getField()).collect(Collectors.toList());

        return new ResponseEntity<>(new TemplateBillList(fields, OffsetDateTime.now(),
                errors), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ConstraintViolationException.class})
    protected ResponseEntity<TemplateBill> handleConstraintViolationException(ConstraintViolationException ex) {
        return new ResponseEntity<>(new TemplateBill(ex.getMessage()
                .substring(ex.getMessage().indexOf(" ") + 1), OffsetDateTime.now(),
                ex.getClass().getSimpleName()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({BillCreateException.class})
    public ResponseEntity<TemplateBill> handleBillCreateException(BillCreateException ex) {
        return new ResponseEntity<>(new TemplateBill(ex.getMessage(), OffsetDateTime.now(),
                ex.getClass().getSimpleName()), HttpStatus.NOT_FOUND);
    }

}
