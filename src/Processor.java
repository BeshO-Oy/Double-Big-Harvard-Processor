import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Processor {
	
	private int cycles ;
	private short PC ;
	private ArrayList<Instruction> instructionMemory;
	private byte[] dataMemory;
	private Register[] generalPurposeRegisters; 
	private StatusRegister statusRegister;
	private Instruction[] pipelineInstructions;
	private String pipelineGUI;
	private boolean isBranch;
	private int numCycles;
	
	public Processor() {
	
		generalPurposeRegisters = new Register[64];
        for (int i = 0; i < 64; i++) {
            generalPurposeRegisters[i] = new Register((byte) 0);
        }
        

        statusRegister = new StatusRegister();
        statusRegister.clearFlags();
        
        PC = 0;
        instructionMemory = new ArrayList<Instruction>(1024);
        dataMemory=new byte[2048];
        this.cycles=1;
        
        pipelineInstructions = new Instruction[3];
        
        isBranch=false;
        
	}
	
	public void fetch() {
	    if (PC < instructionMemory.size()) {
	        pipelineInstructions[2] = pipelineInstructions[1];
	        pipelineInstructions[1] = pipelineInstructions[0];
	        pipelineInstructions[0] = instructionMemory.get(PC);
	        
	        System.out.println( "FetchInput : " + PC + " FetchOutput : " + instructionMemory.get(PC));
	        
	        PC++;
	    } else {
	        // No more instructions to fetch
	        pipelineInstructions[2] = pipelineInstructions[1];
	        pipelineInstructions[1] = pipelineInstructions[0];
	        pipelineInstructions[0] = null;
	    }
	}
	
	public void decode(){
		
		Instruction instructionToDecode = pipelineInstructions[1];
	
		instructionToDecode.setOpcode((instructionToDecode.getValue() >> 12) & 0b1111); 
		instructionToDecode.setOperand1((instructionToDecode.getValue() >> 6) & 0b111111); 
		//instructionToDecode.setOperand2(instructionToDecode.getValue() & 0b111111); 
		
		int operand2Bits = instructionToDecode.getValue() & 0b111111;

		String s = "";
	    if ((operand2Bits & 0b100000) != 0) {
	        int signExtendedValue = operand2Bits | 0b111111000000;
	        instructionToDecode.setOperand2(signExtendedValue);
	        s="Immediate -> ";
	    } else {
	    	s= "Operand2 -> ";
	        instructionToDecode.setOperand2(operand2Bits);
	    }
		
        System.out.println( "DecodeInput : " + instructionToDecode + " DecodeOutput : " + "Opcode -> " + instructionToDecode.getOpcode() + " , "
        																   + "Operand1 -> " + instructionToDecode.getOperand1() + " , " 
        																   + s + instructionToDecode.getOperand2() + " , ");

	}
	
	public void execute() {
		
		statusRegister.clearFlags();
		
		Instruction instructionToExecute = pipelineInstructions[2];
		
		aluExecution(instructionToExecute.getOpcode(), instructionToExecute.getOperand1(), instructionToExecute.getOperand2());
		
		System.out.println("ExecuteInput : "  + "Opcode -> " + instructionToExecute.getOpcode() + " , "
        				               + "Operand1 -> " + instructionToExecute.getOperand1() + " , " 
        					           + "Operand2 -> "+ instructionToExecute.getOperand2() + " , " );
		
	}
	
	public void aluExecution(int opcode, int operand1, int operand2) {
		
		switch(opcode){
		
		case 0 :
			add(operand1,operand2);
			break;
			
		case 1:
			sub(operand1,operand2);
			break;
			
		case 2:
			mul(operand1,operand2);
			break;
			
		case 3:
			movi(operand1,operand2);
			break;
			
		case 4:
			beqz(operand1,operand2);
			break;
			
		case 5:
			andi(operand1,operand2);
			break;
		
		case 6:
			eor(operand1,operand2);
			break;	
			
		case 7:
			br(operand1,operand2);
			break;
			
		case 8:
			sal(operand1,operand2);
			break;	
		
		case 9:
			sar(operand1,operand2);
			break;
		
		case 10:
			ldr(operand1,operand2);
			break;
			
		case 11:
			str(operand1,operand2);
			break;	
			
			
		}
		
	}
	
	public void runProgram(String fileName) {
		
		readInstructions(fileName);
		
        int numInstructions = Math.min(instructionMemory.size(),1024);
        numCycles = 3 + ((numInstructions - 1) * 1);
        int x=0;
    	int y=0;
        
        for (this.cycles = 1; cycles <= numCycles; cycles++) {
        	
        	
        	System.out.println("Clock Cycle No : " + cycles);
        	pipelineGUI+="Clock Cycle No : " + cycles + "\n" ;
        	
        
        	
        	if(!isBranch)
        		fetch();
        	else {
        		pipelineInstructions[0]= instructionMemory.get(PC);
        		pipelineInstructions[1]=null;
    			pipelineInstructions[2]=null;
        		isBranch=false;
        		x=1;
        		y=2;
        		PC++;
        	}
            
        	
        	if(x==0) {
        		if (cycles >= 2 && cycles < numCycles )
                    decode();
        	}
    		else
    			x--;
        	
        	if(y==0) {
        		if(cycles > 2 && y==0) 
            		execute();
        	}
        	else 
        		y--;
        	
        	
        	
        	System.out.print(printPipelineInstructions());
        	System.out.println("------------------------");
        	
        	
        	pipelineGUI+=printPipelineInstructions() + "\n";
        	pipelineGUI+= "-------------------------------------" +"\n";
        	
        }     	
        
    }
	

	private void readInstructions(String fileName) {
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
	        String line;
	        while ((line = br.readLine()) != null) {
	            Instruction instruction = parseInstruction(line);
	            instructionMemory.add(instruction);
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
		
	}

	private Instruction parseInstruction(String line) {
		
		 	String[] parts = line.split(" ");
		    String opcode = parts[0].toUpperCase();
		    String operand1 = parts[1];
		    String operand2 = parts[2];
		    
		    
		    int opcodePart =0;
		    switch (opcode.toUpperCase()) {
		    case "ADD":
		        opcodePart = 0;
		        break;
		    case "SUB":
		        opcodePart = 1;
		        break;
		    case "MUL":
		        opcodePart = 2;
		        break;
		    case "MOVI":
		        opcodePart = 3;
		        break;
		    case "BEQZ":
		        opcodePart = 4;
		        break;
		    case "AND":
		        opcodePart = 5;
		        break;
		    case "EOR":
		        opcodePart = 6;
		        break;
		    case "BR":
		        opcodePart = 7;
		        break;
		    case "SAL":
		        opcodePart = 8;
		        break;
		    case "SAR":
		        opcodePart = 9;
		        break;
		    case "LDR":
		        opcodePart = 10;
		        break;
		    case "STR":
		        opcodePart = 11;
		        break;
		    default:
		        throw new IllegalArgumentException("Invalid opcode: " + opcode);
		}

		    int operand1Part = Integer.parseInt(operand1.substring(1));
		    int operand2Part = 0;
		    
		    if (opcode.equalsIgnoreCase("add") || opcode.equalsIgnoreCase("sub") || opcode.equalsIgnoreCase("mul") 
		    		|| opcode.equalsIgnoreCase("eor") || opcode.equalsIgnoreCase("br"))
		     operand2Part = Integer.parseInt(operand2.substring(1)); //if R-format
		    else 
		    	 operand2Part= Integer.parseInt(operand2); //if I-format

				 int fullInst = opcodePart << 6;
		    	 fullInst |= operand1Part;
		    	 fullInst <<= 6;
		    	 fullInst |= (operand2Part&0b111111);
		    
		    
		    Instruction instruction = new Instruction((short) fullInst);
		   
		    
		    return instruction;
	
	}

	private void add(int operand1, int operand2) {
			
		Register R1=generalPurposeRegisters[operand1];
		Register R2=generalPurposeRegisters[operand2];
			
		
		
		int result = R1.getValue() + R2.getValue();
		
		System.out.println("ExecuteOutput :" + result);
		//pipelineGUI+= "ExecuteOutput :" + result + "\n" ;
		
		int R1Unsigned = (R1.getValue() & 0xFF);
	    int R2Unsigned = (R2.getValue() & 0xFF); 
	    
//	    int resultUnsigned = (R1.getValue() + R2.getValue()) & 0xFF ;
	   
	    
	   
	    if (((R1Unsigned + R2Unsigned) & 0x100) == 0x100) {
	        statusRegister.setCarryFlag();
	    }
	    

//		if((R1.getValue()>=0 && R2.getValue()>=0 && result<0) ||
//				(R1.getValue()<0 && R2.getValue()<0 && result>=0)) {
//			
//			statusRegister.setOverflowFlag();
//		}
	    
	    if (checkOverflow(R1.getValue(), R2.getValue()))
	    	statusRegister.setOverflowFlag();
		
		if(result < 0) {
			statusRegister.setNegativeFlag();
		}
		
		statusRegister.setSignFlag();
		
		if(result==0) {
			statusRegister.setZeroFlag();
		}
		
		System.out.println("Register"+ operand1 + " value was changed to " + generalPurposeRegisters[operand1].getValue() );
		//pipelineGUI += "Register"+ operand1 + " value was changed to " + generalPurposeRegisters[operand1].getValue() + "\n";
		
		generalPurposeRegisters[operand1].setValue((byte)(R1.getValue()+R2.getValue()));
	}
	
	private boolean checkOverflow(int r1 , int r2 ) {
		
		int carry6to7 = ((r1 & 0b01000000) >> 6) | ((r2 & 0b01000000) >> 6);

		int carry7 = ((r1 & 0b10000000) >> 7) | ((r2 & 0b10000000) >> 7);

		int overflow = carry6to7 ^ carry7;
		
		if (overflow == 0b1)
			return true ;
		else
			return false;
	
	}

	private void sub(int operand1, int operand2) {

		Register R1=generalPurposeRegisters[operand1];
		Register R2=generalPurposeRegisters[operand2];
		
		
		int result = R1.getValue()- R2.getValue();
		
		System.out.println("ExecuteOutput :" + result);

		
		if((R1.getValue()>0 && R2.getValue()<0 && result<0) ||
				(R1.getValue()<0 && R2.getValue()>0 && result>0)) {
			statusRegister.setOverflowFlag();
		}
		
//		 if (checkOverflow(R1.getValue(), R2.getValue()))
//		    	statusRegister.setOverflowFlag();
		
		if(result < 0) {
			statusRegister.setNegativeFlag();
		}
		
		statusRegister.setSignFlag();
		
		if(result==0) {
			statusRegister.setZeroFlag();
		}
		
		System.out.println("Register"+ operand1 + " value was changed to " + generalPurposeRegisters[operand1].getValue() );
		
		generalPurposeRegisters[operand1].setValue((byte)(R1.getValue()-R2.getValue()));
	}

	private void mul(int operand1, int operand2) {
		
		Register R1=generalPurposeRegisters[operand1];
		Register R2=generalPurposeRegisters[operand2];
		
		
		int result = R1.getValue()*R2.getValue();
		
		System.out.println("ExecuteOutput :" + result);

		
		if(result < 0) {
			statusRegister.setNegativeFlag();
		}
		
		if(result==0) {
			statusRegister.setZeroFlag();
		}
		
		System.out.println("Register"+ operand1 + " value was changed to " + generalPurposeRegisters[operand1].getValue() );
		
		generalPurposeRegisters[operand1].setValue((byte)(R1.getValue()*R2.getValue()));
	}

	private void movi(int operand1, int immediate) {
		
		generalPurposeRegisters[operand1].setValue((byte)immediate);
		
		System.out.println("Register"+ operand1 + " value was changed to " + generalPurposeRegisters[operand1].getValue() );
	}

	private void beqz(int operand1, int immediate) {

		if(generalPurposeRegisters[operand1].getValue()==0) {
			PC+=(immediate-2);
			isBranch= true;
			numCycles-= (immediate-2);
			
		}
	
	}

	private void andi(int operand1, int immediate) {
		
		generalPurposeRegisters[operand1].setValue((byte) (operand1&immediate));
		
		int result = generalPurposeRegisters[operand1].getValue();
		
		System.out.println("ExecuteOutput :" + result);

		
		if(result < 0) {
			statusRegister.setNegativeFlag();
		}
		
		if(result==0) {
			statusRegister.setZeroFlag();
		}
		
		System.out.println("Register"+ operand1 + " value was changed to " + generalPurposeRegisters[operand1].getValue() );
	}

	private void eor(int operand1, int operand2) {

		generalPurposeRegisters[operand1].setValue((byte) (operand1^operand2));
		
		int result = generalPurposeRegisters[operand1].getValue();
		
		System.out.println("ExecuteOutput :" + result);

		
		if(result < 0) {
			statusRegister.setNegativeFlag();
		}
		
		if(result==0) {
			statusRegister.setZeroFlag();
		}
		
		System.out.println("Register"+ operand1 + " value was changed to " + generalPurposeRegisters[operand1].getValue() );
	}

	private void br(int operand1, int operand2) {
		
		Register R1=generalPurposeRegisters[operand1];
		Register R2=generalPurposeRegisters[operand2];
		numCycles-= ((R1.getValue() << 4) | R2.getValue())-PC;
		PC =  (short) ((R1.getValue() << 4) | R2.getValue());
		
		isBranch= true;
		
	}

	private void sal(int operand1, int immediate) {
		
		Register R1=generalPurposeRegisters[operand1];
		generalPurposeRegisters[operand1].setValue((byte) (R1.getValue()<<immediate));
		
		int result = generalPurposeRegisters[operand1].getValue();
		
		System.out.println("ExecuteOutput :" + result);

		
		if(result < 0) {
			statusRegister.setNegativeFlag();
		}
		
		if(result==0) {
			statusRegister.setZeroFlag();
		}
		
		System.out.println("Register"+ operand1 + " value was changed to " + generalPurposeRegisters[operand1].getValue() );
	}

	private void sar(int operand1, int immediate) {
		
		Register R1=generalPurposeRegisters[operand1];
		generalPurposeRegisters[operand1].setValue((byte) (R1.getValue()>>immediate));
		
		int result = generalPurposeRegisters[operand1].getValue();
		
		System.out.println("ExecuteOutput :" + result);

		
		if(result < 0) {
			statusRegister.setNegativeFlag();
		}
		
		if(result==0) {
			statusRegister.setZeroFlag();
		}
		
		System.out.println("Register"+ operand1 + " value was changed to " + generalPurposeRegisters[operand1].getValue() );
	}

	private void ldr(int operand1, int immediate) {

		generalPurposeRegisters[operand1].setValue(dataMemory[immediate]);
		
		System.out.println("Register"+ operand1 + " value was changed to " + generalPurposeRegisters[operand1].getValue() );
		
	}

	private void str(int operand1, int immediate) {

		dataMemory[immediate]=generalPurposeRegisters[operand1].getValue();
		
		System.out.println("The Data in the address " + immediate + " was changed to " + generalPurposeRegisters[operand1].getValue());
	}

	
	public String printRegisters() {
		String s = "";
		for(int i=0; i< generalPurposeRegisters.length;i++) {
			s+= "GeneralPurposeRegister" + i + ": " +generalPurposeRegisters[i] + "\n"; 
		}
		s+= "PC: " + PC +"\n";
		s+= "SREG: " + statusRegister + "\n";
		return s;
	}
	
	public String printPipelineInstructions() {
		String s = "";
		for(int i=0; i< pipelineInstructions.length;i++) {
			String stage = i==0? "Fetch :" : i==1? "Decode :" : "Execute :" ; 
			s+= stage + pipelineInstructions[i] + "\n" ; 
		}
		
		return s;
	}
	
	public String printMainMemory() {
		String MainMemory ="InstructionMemory :- " + "\n";
		for(int i=0; i< instructionMemory.size();i++) {
			MainMemory+= "Instruction"+i+": " +instructionMemory.get(i)+ "\n" ;
		}
		MainMemory+="--------------------------------" + "\n";
		MainMemory+= "DataMemory :- " + "\n";
		
		for(int j=0; j<dataMemory.length; j++) {
			MainMemory+= "Data"+j+": "+ dataMemory[j] + "\n";
		}
		return MainMemory;
	}
	 
	
	

	public String getPipelineGUI() {
		return pipelineGUI;
	}

	public void setPipelineGUI(String pipelineGUI) {
		this.pipelineGUI = pipelineGUI;
	}
	
	public static void main(String[] args) {
		
		Processor p = new Processor();
	
		p.runProgram("src/Program.txt");
		
		System.out.println(p.printRegisters());
		System.out.println(p.printMainMemory());
		
	
	}
	
	

}
