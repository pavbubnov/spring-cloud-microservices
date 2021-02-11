package com.javastart.transfer.exception;

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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;


@ControllerAdvice
public class TransferServiceExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatus status,
                                                                  WebRequest request) {
        BindingResult result = ex.getBindingResult();
        List<String> errors = result.getFieldErrors().stream().map(objectError ->
                ((FieldError) objectError).getDefaultMessage()).collect(Collectors.toList());
        List<String> fields = result.getFieldErrors().stream().map(objectError ->
                ((FieldError) objectError).getField()).collect(Collectors.toList());
        return new ResponseEntity<>(new ValidateException(fields, OffsetDateTime.now(),
                errors), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({RollbackException.class})
    public ResponseEntity<HandlerTransferException> handleRollbackException(RollbackException ex) {
        return new ResponseEntity<>(new HandlerTransferException(ex.getMessage(), OffsetDateTime.now(),
                ex.getClass().getSimpleName()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({NoRollbackException.class})
    public ResponseEntity<HandlerTransferException> handleNoRollbackException(NoRollbackException ex) {
        return new ResponseEntity<>(new HandlerTransferException(ex.getMessage(), OffsetDateTime.now(),
                ex.getClass().getSimpleName()), HttpStatus.NOT_FOUND);
    }

}
