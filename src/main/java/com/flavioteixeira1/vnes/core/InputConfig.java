package com.flavioteixeira1.vnes.core;

public class InputConfig {
    public static final int HANDLER_DISABLED = 0;
    public static final int HANDLER_KEYBOARD = 1;
    public static final int HANDLER_JOYSTICK = 2;
    
    public static String getHandlerName(int handlerType) {
        switch (handlerType) {
            case HANDLER_KEYBOARD: return "Teclado";
            case HANDLER_JOYSTICK: return "Joystick";
            case HANDLER_DISABLED: return "Desabilitado";
            default: return "Desconhecido";
        }
    }
}