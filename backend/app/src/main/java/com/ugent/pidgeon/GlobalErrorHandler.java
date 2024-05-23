package com.ugent.pidgeon;


import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalErrorHandler {



  private void logError(Exception ex) {
    Logger logger = Logger.getGlobal();
      // Log the error with the logger
    logger.log(Level.SEVERE, ex.getMessage(), ex);
  }

  /* Gets thrown when a invalid json is sent */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiErrorReponse> handleHttpMessageNotReadableException(HttpServletRequest request, Exception ex) {
    logError(ex);
    String path = request.getRequestURI();
    HttpStatus status = HttpStatus.BAD_REQUEST;
    return ResponseEntity.status(status).body(new ApiErrorReponse(OffsetDateTime.now(), status.value(),status.getReasonPhrase(),
        "Unable to process the request due to invalid or missing data. Please ensure the request body is properly formatted and all required fields are provided.", path));
  }

  /* Gets thrown when endpoint doesn't exist */
  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<ApiErrorReponse> handleNoHandlerFoundException(HttpServletRequest request, Exception ex) {
    logError(ex);
    String path = request.getRequestURI();
    HttpStatus status = HttpStatus.NOT_FOUND;
    return ResponseEntity.status(status).body(new ApiErrorReponse(OffsetDateTime.now(), status.value(), status.getReasonPhrase(),
        "Resource/endpoint doesn't exist", path));
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ApiErrorReponse> handleNoResourceFoundException(HttpServletRequest request, Exception ex) {
    logError(ex);
    String path = request.getRequestURI();
    HttpStatus status = HttpStatus.NOT_FOUND;
    return ResponseEntity.status(status).body(new ApiErrorReponse(OffsetDateTime.now(), status.value(), status.getReasonPhrase(),
        "Resource/endpoint doesn't exist", path));
  }

  /* Gets thrown when the method is not allowed */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ApiErrorReponse> handleMethodNotSupportedException(HttpServletRequest request, Exception ex) {
    logError(ex);
    String path = request.getRequestURI();
    HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;
    return ResponseEntity.status(status).body(new ApiErrorReponse(OffsetDateTime.now(), status.value(), status.getReasonPhrase(),
        "Method not supported", path));
  }

  /* Gets thrown when u path variable is of the wrong type */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiErrorReponse> handleMethodArgumentTypeMismatchException(HttpServletRequest request, Exception ex) {
    logError(ex);
    String path = request.getRequestURI();
    HttpStatus status = HttpStatus.BAD_REQUEST;
    return ResponseEntity.status(status).body(new ApiErrorReponse(OffsetDateTime.now(), status.value(), status.getReasonPhrase(),
        "Invalid url argument type", path));
  }

  /* Gets thrown when an unexpected error occurs */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorReponse> handleException(HttpServletRequest request, Exception ex) {
    logError(ex);
    String path = request.getRequestURI();
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    return ResponseEntity.status(status).body(new ApiErrorReponse(OffsetDateTime.now(), status.value(), status.getReasonPhrase(),
        "An unexpected error occurred", path));
  }

}
