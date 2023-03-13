package co.ke.proaktivio.qwanguapi.pojos;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderType {
    ASC("ASC"),
    DESC("DESC");
	private final String type;
}
