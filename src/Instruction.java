
public class Instruction {
		private short value;
        private int opcode;
        private	int operand1;
        private int operand2;

        public Instruction(short value) {
        	this.setValue(value);
           
        }

		public int getOpcode() {
			return opcode;
		}

		public void setOpcode(int opcode) {
			this.opcode = opcode;
		}

		public int getOperand1() {
			return operand1;
		}

		public void setOperand1(int operand1) {
			this.operand1 = operand1;
		}

		public int getOperand2() {
			return operand2;
		}

		public void setOperand2(int operand2) {
			this.operand2 = operand2;
		}

		public short getValue() {
			return value;
		}

		public void setValue(short value) {
			this.value = value;
		}

		public String toString() {
			return  Integer.toBinaryString(value) ;
		}

       
    }
