package com.flavioteixeira1.vnes.core;

import java.util.HashMap;
import java.util.Map;

public class DummyInputHandler implements InputHandler {
    @Override
    public void update() {}
    
    @Override
    public boolean isKeyPressed(int keyCode) {
        return false;
    }
    
    @Override
    public void reset() {}
    
    @Override
    public void setKeyBindings(Map<Integer, Integer> keyBindings) {}
    
    @Override
    public Map<Integer, Integer> getKeyBindings() {
        return new HashMap<>();
    }
    
    @Override
    public String getInputType() {
        return "DUMMY";
    }
    
    @Override
    public void mapKey(int padKey, int deviceKey) {}
    
    @Override
    public short getKeyState(int padKey) {
        return 0x40; // Não pressionado
    }

    @Override
    public void handleJoystickKey(int keyCode, boolean pressed) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleJoystickKey'");
    }

    @Override
    public boolean getKeyState2(int key) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getKeyState2'");
    }

    @Override
    public void mapKey2(int key, int gamekey) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mapKey2'");
    }
}