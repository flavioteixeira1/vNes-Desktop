public class ChannelNoise implements PapuChannel{
	
	
	PAPU papu;
	
	public boolean isEnabled;
	public boolean envDecayDisable;
	public boolean envDecayLoopEnable;
	public boolean lengthCounterEnable;
	public boolean envReset;
	public boolean shiftNow;
	boolean sweepActive;
	boolean sweepCarry;
	boolean updateSweepPeriod;
	
	public int lengthCounter;
	public int progTimerCount;
	public int progTimerMax;
	public int envDecayRate;
	public int envDecayCounter;
	public int envVolume;
	public int masterVolume;
	public int shiftReg;
	public int randomBit;
	public int randomMode;
	public int sampleValue;
	public long accValue=0;
	public long accCount=1;
	public int tmp;
	int squareCounter;
	int sweepCounter;
	int sweepCounterMax;
	int sweepMode;
	int sweepShiftAmount;
	int dutyMode;
	int sweepResult;
	int vol;
	
	
	public ChannelNoise(PAPU papu){
		this.papu = papu;
		shiftReg = 1<<14;
	}
	
	public void clockLengthCounter(){
		if(lengthCounterEnable && lengthCounter>0){
			lengthCounter--;
			if(lengthCounter == 0)updateSampleValue();
		}
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


			
	public void clockEnvDecay(){
		
		if(envReset){
			
			// Reset envelope:
			envReset = false;
			envDecayCounter = envDecayRate + 1;
			envVolume = 0xF;
			
		}else if(--envDecayCounter <= 0){
			
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
	
	public void updateSampleValue(){
		if(isEnabled && lengthCounter>0){
			sampleValue = randomBit * masterVolume;
		}
	}
	
	public void writeReg(int address, int value){
		
		if(address == 0x400C){
			
			// Volume/Envelope decay:
			envDecayDisable = ((value&0x10)!=0);
			envDecayRate = value&0xF;
			envDecayLoopEnable = ((value&0x20)!=0);
			lengthCounterEnable = ((value&0x20)==0);
			masterVolume = envDecayDisable?envDecayRate:envVolume;
			
		}else if(address == 0x400E){
			
			// Programmable timer:
			progTimerMax = papu.getNoiseWaveLength(value&0xF);
			randomMode = value>>7;
			
		}else if(address == 0x400F){
			
			// Length counter
			lengthCounter = papu.getLengthMax(value&248);
			envReset = true;
			
		}
		
		// Update:
		//updateSampleValue();
		
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
	
	public void reset(){
		
		progTimerCount = 0;
		progTimerMax = 0;
		isEnabled = false;
		lengthCounter = 0;
		lengthCounterEnable = false;
		envDecayDisable = false;
		envDecayLoopEnable = false;
		shiftNow = false;
		envDecayRate = 0;
		envDecayCounter = 0;
		envVolume = 0;
		masterVolume = 0;
		shiftReg = 1;
		randomBit = 0;
		randomMode = 0;
		sampleValue = 0;
		tmp = 0;
		
	}
	
	public void destroy(){
		papu = null;
	}
	
}