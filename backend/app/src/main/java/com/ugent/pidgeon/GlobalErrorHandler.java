package com.ugent.pidgeon;


import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalErrorHandler {



  private void logError(Exception ex) {
    Logger logger = Logger.getGlobal();
      // Log the error with the logger
    logger.log(Level.SEVERE, ex.getMessage(), ex);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiErrorReponse> handleHttpMessageNotReadableException(HttpServletRequest request, Exception ex) {
    logError(ex);
    String path = request.getRequestURI();
    HttpStatus status = HttpStatus.BAD_REQUEST;
    return ResponseEntity.status(status).body(new ApiErrorReponse(OffsetDateTime.now(), status.value(),status.getReasonPhrase(),
        "Unable to process the request due to invalid or missing data. Please ensure the request body is properly formatted and all required fields are provided.", path));
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ApiErrorReponse> handleHttpMessageNotFoundException(HttpServletRequest request, Exception ex) {
    logError(ex);
    String path = request.getRequestURI();
    HttpStatus status = HttpStatus.NOT_FOUND;
    return ResponseEntity.status(status).body(new ApiErrorReponse(OffsetDateTime.now(), status.value(), status.getReasonPhrase(),
        "Endpoint doesn't exist", path));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorReponse> handleException(HttpServletRequest request, Exception ex) {
    logError(ex);
    String path = request.getRequestURI();
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    return ResponseEntity.status(status).body(new ApiErrorReponse(OffsetDateTime.now(), status.value(), status.getReasonPhrase(),
        "An unexpected error occurred", path));
  }

}
