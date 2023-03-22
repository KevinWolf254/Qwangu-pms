package co.ke.proaktivio.qwanguapi.pojos;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationStatus {
	PENDING("PENDING"), SENT("SENT"), FAILED("FAILED");

	private final String status;
}
