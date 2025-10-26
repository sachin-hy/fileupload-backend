package com.fileupload.fileproject.Exception;


import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalException {


    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception ex)
    {
         log.error("Exception  = {}" , ex.getMessage() );
         Map<String, Object> error = new HashMap<>();

         error.put("message", "SomeThing Went Wrong !Please Try After Sometime");

         return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(FileNotReadyException.class)
    public ResponseEntity<?> handleFileNotReadyException(Exception ex)
    {
        log.error("FileNotReadyException = {} " ,ex.getMessage());

        Map<String, Object> error= new HashMap<>();

        error.put("message", ex.getMessage());

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(FileExpiredException.class)
    public ResponseEntity<?> handleFileExpiredException(Exception ex)
    {
        log.error("FileExpiredException = {} " ,ex.getMessage());

        Map<String, Object> error = new HashMap<>();

        error.put("message", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(InternalError.class)
    public ResponseEntity<?> handleInternalError(InternalError e)
    {
        log.error("InternalError = {}" , e.getMessage());

        Map<String,Object> error = new HashMap<>();
        error.put("message", e.getMessage());

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UserAlreadyPresent.class)
    public ResponseEntity<?> handleUserAlreadyPresent(UserAlreadyPresent ex)
    {
        log.error("UserAlreadyPresent = {}" , ex.getMessage());
        Map<String, Object> error = new HashMap<>();
        error.put("message", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }


    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<?> handleUsernameNotFoundException(UsernameNotFoundException ex)
    {
        log.error("usernamenotfound execption {}" , ex.getMessage());
        Map<String, Object> error = new HashMap<>();
        error.put("message", ex.getMessage());

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);


    }

}
