package dev.gavin.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class ResourceAlreadyExistException extends RuntimeException {

    public ResourceAlreadyExistException(String resourceName, String fieldName, String fieldValue) {
        super(String.format("%s with %s %s already exists", resourceName, fieldName, fieldValue));
    }

    public ResourceAlreadyExistException(String errorMessage) {
        super(errorMessage);
    }

}
