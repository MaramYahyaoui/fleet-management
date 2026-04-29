package com.flotte.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponseDTO<T> {
	private boolean success;
	private String message;
	private T data;

	public static <T> ApiResponseDTO<T> ok(String message, T data) {
		return ApiResponseDTO.<T>builder().success(true).message(message).data(data).build();
	}

	public static <T> ApiResponseDTO<T> ok(T data) {
		return ok("Opération réussie", data);
	}

	public static <T> ApiResponseDTO<T> error(String message) {
		return ApiResponseDTO.<T>builder().success(false).message(message).build();
	}
}
