package com.core.libraries.exceptionhandler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import com.core.libraries.FieldValidationError;
import com.core.libraries.ValidationException;

/**
 * Controller advice to translate the server side exception to client-friendly
 * JSON structures
 */

@ControllerAdvice
public class ExceptionTranslator {

	static String LOG_ERROR = "Returning ErrorDTO due to: {}";
	// static Logger logger = LoggerFactory.getLogger(ExceptionTranslator.class);

	@ExceptionHandler(ConcurrencyFailureException.class)
	@ResponseStatus(HttpStatus.EXPECTATION_FAILED)
	@ResponseBody
	public ErrorDTO processConcurrencyFailureException(ConcurrencyFailureException ex) {
		return new ErrorDTO(ErrorConstants.ERR_CONCURRENCY_FAILURE_STATUS, ErrorConstants.ERR_CONCURRENCY_FAILURE);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	@ResponseStatus(HttpStatus.EXPECTATION_FAILED)
	@ResponseBody
	public ErrorDTO processMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
		return new ErrorDTO(ErrorConstants.ERR_METHOD_NOT_SUPPORTED_STATUS, ErrorConstants.ERR_METHOD_NOT_SUPPORTED);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public ErrorDTO processMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
		BindingResult result = ex.getBindingResult();
		List<FieldErrorDTO> fieldErrors = processFieldErrors(result.getFieldErrors());
		return new ErrorDTO(ErrorConstants.ERR_VALIDATION_STATUS, ErrorConstants.ERR_VALIDATION,
				ErrorConstants.ERR_VALIDATION, fieldErrors);
	}

	@ExceptionHandler(BindException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public ErrorDTO processBindException(BindException ex) {

		BindingResult result = ex.getBindingResult();
		List<FieldErrorDTO> fieldErrors = processFieldErrors(result.getFieldErrors());
		return new ErrorDTO(ErrorConstants.ERR_VALIDATION_STATUS, ErrorConstants.ERR_VALIDATION,
				ErrorConstants.ERR_VALIDATION, fieldErrors);
	}

	@ExceptionHandler(AuthenticationException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	@ResponseBody
	public ErrorDTO processAuthenticationException(AuthenticationException ex) {
		return new ErrorDTO(ErrorConstants.ERR_UNAUTHORIZED_STATUS, ex.getMessage(), ErrorConstants.ERR_UNAUTHORIZED);
	}

	@ExceptionHandler(AccessDeniedException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	@ResponseBody
	public ErrorDTO processAccessDeniedException(AccessDeniedException ex) {
		return new ErrorDTO(ErrorConstants.ERR_ACCESS_DENIED_STATUS, ex.getMessage(),
				ErrorConstants.ERR_ACCESSS_DENIED);
	}

	@ExceptionHandler(CustomException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public ParameterizedErrorDTO processCustomException(CustomException ex) {
		return ex.getErrorDTO();
	}

	@ExceptionHandler(ValidationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public ErrorDTO processValidationException(ValidationException ex) {
		List<FieldErrorDTO> fieldErrors = processFieldValidationError(ex.getValidationErrors());
		return new ErrorDTO(ErrorConstants.ERR_VALIDATION_STATUS, ErrorConstants.ERR_VALIDATION,
				ErrorConstants.ERR_VALIDATION, fieldErrors);
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public ErrorDTO processMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
		return new ErrorDTO(ErrorConstants.ERR_VALIDATION_STATUS, ErrorConstants.ERR_MAX_UPLOAD_SIZE_EXCEEDED);
	}

	@ExceptionHandler(MultipartException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public ErrorDTO processMultipartException(MultipartException ex) {
		return new ErrorDTO(ErrorConstants.ERR_VALIDATION_STATUS, ErrorConstants.ERR_MULTIPART_FAILURE);
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	@ResponseBody
	public ErrorDTO processRuntimeException(RuntimeException ex) {
		return new ErrorDTO(ErrorConstants.ERR_RUNTIME_FAILURE_STATUS, ErrorConstants.ERR_RUNTIME_FAILURE);
	}

	List<FieldErrorDTO> processFieldErrors(List<FieldError> fieldErrors) {
		List<FieldErrorDTO> fieldErrorDtoList = new ArrayList<>();

		for (FieldError fieldError : fieldErrors) {
			FieldErrorDTO fieldErrorDto = new FieldErrorDTO("", fieldError.getField(), fieldError.getDefaultMessage());
			fieldErrorDtoList.add(fieldErrorDto);
		}

		return fieldErrorDtoList;
	}

	List<FieldErrorDTO> processFieldValidationError(List<FieldValidationError> fieldValidationErrors) {
		List<FieldErrorDTO> fieldErrorDtoList = null;
		if (fieldValidationErrors == null) {
			return fieldErrorDtoList;
		}

		fieldErrorDtoList = new ArrayList<>();
		for (FieldValidationError fieldValidationError : fieldValidationErrors) {
			FieldErrorDTO fieldErrorDto = new FieldErrorDTO("", fieldValidationError.getField(),
					fieldValidationError.getMessage());
			fieldErrorDtoList.add(fieldErrorDto);
		}
		return fieldErrorDtoList;
	}

}