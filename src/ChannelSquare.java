public class ChannelSquare implements PapuChannel{
	
	PAPU papu;
	
	static int[] dutyLookup;
	static int[] impLookup;
	
	boolean sqr1;
	boolean isEnabled;
	boolean lengthCounterEnable;
	boolean sweepActive;
	boolean envDecayDisable;
	boolean envDecayLoopEnable;
	boolean envReset;
	boolean sweepCarry;
	boolean updateSweepPeriod;
	
	int progTimerCount;
	int progTimerMax;
	int lengthCounter;
	int squareCounter;
	int sweepCounter;
	int sweepCounterMax;
	int sweepMode;
	int sweepShiftAmount;
	int envDecayRate;
	int envDecayCounter;
	int envVolume;
	int masterVolume;
	int dutyMode;
	int sweepResult;
	int sampleValue;
	int vol;
	
	
	public ChannelSquare(PAPU papu, boolean square1){
		
		this.papu = papu;
		sqr1 = square1;
		
	}
	
	public void clockLengthCounter(){
		
		if(lengthCounterEnable && lengthCounter>0){
			lengthCounter--;
			if(lengthCounter==0)updateSampleValue();
		}
		
	}
	
	public void clockEnvDecay(){
		
		if(envReset){
			
			// Reset envelope:
			envReset = false;
			envDecayCounter = envDecayRate + 1;
			envVolume = 0xF;
			
		}else if((--envDecayCounter) <= 0){
			
			// Normal handling:
			envDecayCounter = envDecayRate + 1;
			if(envVolume>0){
				envVolume--;
			}else{
				envVolume = envDecayLoopEnable ? 0xF : 0;
			}
			
		}
		
		masterVolume = envDecayDisable ? envDecayRate : envVolume;
		updateSampleValue();

	}
	
	public void clockSweep(){
		
		if(--sweepCounter<=0){
			
			sweepCounter = sweepCounterMax + 1;
			if(sweepActive && sweepShiftAmount>0 && progTimerMax>7){
				
				// Calculate result from shifter:
				sweepCarry = false;
				if(sweepMode==0){
					progTimerMax += (progTimerMax>>sweepShiftAmount);
					if(progTimerMax > 4095){
						progTimerMax = 4095;
						sweepCarry = true;
					}
				}else{
					progTimerMax = progTimerMax - ((progTimerMax>>sweepShiftAmount)-(sqr1?1:0));
				}
				
			}
				
		}
		
		if(updateSweepPeriod){
			updateSweepPeriod = false;
			sweepCounter = sweepCounterMax + 1;
		}
		
	}
	
	public void updateSampleValue(){
		
		if(isEnabled && lengthCounter>0 && progTimerMax>7){
			
			if(sweepMode==0 && (progTimerMax + (progTimerMax>>sweepShiftAmount)) > 4095){
			//if(sweepCarry){
				
				sampleValue = 0;
				
			}else{
			
				sampleValue = masterVolume*dutyLookup[(dutyMode<<3)+squareCounter];
				
			}
			
		}else{
			
			sampleValue = 0;
			
		}
		
	}
	
	public void writeReg(int address, int value){
		
		int addrAdd = (sqr1?0:4);
		if(address == 0x4000+addrAdd){
			
			// Volume/Envelope decay:
			envDecayDisable = ((value&0x10)!=0);
			envDecayRate = value & 0xF;
			envDecayLoopEnable = ((value&0x20)!=0);
			dutyMode = (value>>6)&0x3;
			lengthCounterEnable = ((value&0x20)==0);
			masterVolume = envDecayDisable?envDecayRate:envVolume;
			updateSampleValue();
			
		}else if(address == 0x4001+addrAdd){
			
			// Sweep:
			sweepActive = ((value&0x80)!=0);
			sweepCounterMax = ((value>>4)&7);
			sweepMode = (value>>3)&1;
			sweepShiftAmount = value&7;
			updateSweepPeriod = true;
			
		}else if(address == 0x4002+addrAdd){
			
			// Programmable timer:
			progTimerMax &= 0x700;
			progTimerMax |= value;
			
		}else if(address == 0x4003+addrAdd){
			
			// Programmable timer, length counter
			progTimerMax &= 0xFF;
			progTimerMax |= ((value&0x7)<<8);
			
			if(isEnabled){
				lengthCounter = papu.getLengthMax(value&0xF8);
			}
			
			envReset  = true;
			
		}
		
	}
	
	public void setEnabled(boolean value){
		isEnabled = value;
		if(!value)lengthCounter = 0;
		updateSampleValue();
	}
	
	public boolean isEnabled(){
		return isEnabled;
	}
	
	public int getLengthStatus(){
		return ((lengthCounter==0 || !isEnabled)?0:1);
	}


	public void stateSave(ByteBuffer buf) {
			buf.putBoolean(isEnabled);
			buf.putBoolean(lengthCounterEnable);
			buf.putBoolean(sweepActive);
			buf.putBoolean(envDecayDisable);
			buf.putBoolean(envDecayLoopEnable);
			buf.putBoolean(envReset);
			buf.putBoolean(sweepCarry);
			buf.putBoolean(updateSweepPeriod);
			
			buf.putInt(progTimerCount);
			buf.putInt(progTimerMax);
			buf.putInt(lengthCounter);
			buf.putInt(squareCounter);
			buf.putInt(sweepCounter);
			buf.putInt(sweepCounterMax);
			buf.putInt(sweepMode);
			buf.putInt(sweepShiftAmount);
			buf.putInt(envDecayRate);
			buf.putInt(envDecayCounter);
			buf.putInt(envVolume);
			buf.putInt(masterVolume);
			buf.putInt(dutyMode);
			buf.putInt(sweepResult);
			buf.putInt(sampleValue);
			buf.putInt(vol);
		}

public void stateLoad(ByteBuffer buf) {
			isEnabled = buf.readBoolean();
			lengthCounterEnable = buf.readBoolean();
			sweepActive = buf.readBoolean();
			envDecayDisable = buf.readBoolean();
			envDecayLoopEnable = buf.readBoolean();
			envReset = buf.readBoolean();
			sweepCarry = buf.readBoolean();
			updateSweepPeriod = buf.readBoolean();
			
			progTimerCount = buf.readInt();
			progTimerMax = buf.readInt();
			lengthCounter = buf.readInt();
			squareCounter = buf.readInt();
			sweepCounter = buf.readInt();
			sweepCounterMax = buf.readInt();
			sweepMode = buf.readInt();
			sweepShiftAmount = buf.readInt();
			envDecayRate = buf.readInt();
			envDecayCounter = buf.readInt();
			envVolume = buf.readInt();
			masterVolume = buf.readInt();
			dutyMode = buf.readInt();
			sweepResult = buf.readInt();
			sampleValue = buf.readInt();
			vol = buf.readInt();
		}
	
	public void reset(){
		
		progTimerCount = 0;
		progTimerMax = 0;
		lengthCounter = 0;
		squareCounter = 0;
		sweepCounter = 0;
		sweepCounterMax = 0;
		sweepMode = 0;
		sweepShiftAmount = 0;
		envDecayRate = 0;
		envDecayCounter = 0;
		envVolume = 0;
		masterVolume = 0;
		dutyMode = 0;
		vol = 0;
		
		isEnabled = false;
		lengthCounterEnable = false;
		sweepActive = false;
		sweepCarry = false;
		envDecayDisable = false;
		envDecayLoopEnable = false;
		
	}
	
	public void destroy(){
		papu = null;
	}
	
	static{
		
		dutyLookup = new int[]{
			 0, 1, 0, 0, 0, 0, 0, 0,
			 0, 1, 1, 0, 0, 0, 0, 0,
			 0, 1, 1, 1, 1, 0, 0, 0,
			 1, 0, 0, 1, 1, 1, 1, 1,
		};
		
		impLookup = new int[]{
			 1,-1, 0, 0, 0, 0, 0, 0,
			 1, 0,-1, 0, 0, 0, 0, 0,
			 1, 0, 0, 0,-1, 0, 0, 0,
			-1, 0, 1, 0, 0, 0, 0, 0,
		};
		
	}
	
}