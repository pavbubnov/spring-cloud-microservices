package com.javastart.account.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;


@ControllerAdvice
public class AccountServiceExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid (MethodArgumentNotValidException ex,
                                                                   HttpHeaders headers, HttpStatus status,
                                                                   WebRequest request) {
        BindingResult result = ex.getBindingResult();
        List<String> errors = result.getFieldErrors().stream().map(objectError -> ((FieldError) objectError).getDefaultMessage()).collect(Collectors.toList());
        return new ResponseEntity<>(new ValidateException(errors), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({AccountNotFoundException.class})
    public ResponseEntity<HandlerAccountException> handleAccountNotFoundException(AccountNotFoundException ex) {
        return new ResponseEntity<>(new HandlerAccountException(ex.getMessage()),HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({CreateBillExceptiton.class})
    public ResponseEntity<HandlerAccountException> handleAccountNotFoundException(CreateBillExceptiton ex) {
        return new ResponseEntity<>(new HandlerAccountException(ex.getMessage()),HttpStatus.NOT_FOUND);
    }
}
