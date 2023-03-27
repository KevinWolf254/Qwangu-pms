package co.ke.proaktivio.qwanguapi.pojos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChargeDto {
	private String name;
	private double amount;
	
	public static class ChargeBuilder {
		private String name;
		private double amount;
		
		public ChargeBuilder name(String name) {
			this.name = name;
			return this;
		}
		public ChargeBuilder amount(double amount) {
			this.amount = amount;
			return this;
		}
		public ChargeDto build() {
			var charge = new ChargeDto();
			charge.setName(name);
			charge.setAmount(amount);
			return charge;
		}
	}
}
