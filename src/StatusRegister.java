
public class StatusRegister {
    private byte register;

    public StatusRegister() {
        register = 0;
    }

    public byte getValue() {
        return register;
    }

    public void setValue(byte value) {
        register = value;
    }

 

    public void setCarryFlag() {
        register |= 0b00010000;
    }

    public void setOverflowFlag() {
    	
        register |= 0b00001000;
    }

    public void setNegativeFlag() {
        register |= 0b00000100;
    }

    public void setSignFlag() {
    	
        byte negativeBit = (byte) ((register >> 2) & 1) ;
        byte overflowBit = (byte) ((register >> 3) & 1) ;
        
        byte signBit =  (byte) (negativeBit ^ overflowBit);
        if (signBit ==0b1) {
        	register|=0b00000010 ;
        }
        
    }

    public void setZeroFlag() {
        register |= 0b00000001;
    }

    public void clearFlags() {
        register &= 0b00000000;
    }
    
    public String toString() {
    	return Integer.toBinaryString(register);
    }
}
