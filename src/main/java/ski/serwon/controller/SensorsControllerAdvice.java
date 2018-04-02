package ski.serwon.controller;

import org.springframework.hateoas.VndErrors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ski.serwon.exception.SensorNotFoundException;
import ski.serwon.exception.ValueOutOfBoundsException;

@RestControllerAdvice
public class SensorsControllerAdvice {
    @ResponseBody
    @ExceptionHandler(SensorNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    VndErrors sensorNotFoundExceptionHandler(SensorNotFoundException e) {
        return new VndErrors("sensor not found", e.getMessage());
    }


    @ResponseBody
    @ExceptionHandler(NumberFormatException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    VndErrors numberFormatExceptionHandler(NumberFormatException e) {
        return new VndErrors("wrong type in request content", e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(ValueOutOfBoundsException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    VndErrors valueOutOfBoundsException(ValueOutOfBoundsException e) {
        return new VndErrors("out of bounds", e.getMessage());
    }
}
