package com.flavioteixeira1.vnes.core.joyrobot.test;

import java.util.Scanner;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Version;

/**
 * 
 * @author endolf 
 * from: http://www.java-gaming.org/index.php/topic,16866.0
 * 
 */

public class JInputJoystickTest {

    private int choice;

    public void showControllerInfo() throws InterruptedException{
        Scanner scanner = new Scanner(System.in);
         Controller[] controllersList = ControllerEnvironment.getDefaultEnvironment().getControllers();
         for(int q =0;q<controllersList.length;q++){
            System.out.println(q + "  " + controllersList[q].getName() + controllersList[q].getType().toString());
        }
        System.out.println("Enter the controller Number u want to show components");
        choice = scanner.nextInt();
        Component[] components = controllersList[choice].getComponents();
        for(int j=0; j<components.length; j++){
                System.out.println("");
                
                // Get the components name
                System.out.println("Component "+j+": "+components[j].getName());
                // Get it's identifier, E.g. BUTTON.PINKIE, AXIS.POV and KEY.Z, 
                System.out.println("    Identifier: "+ components[j].getIdentifier().getName());
                System.out.print("    ComponentType: ");
                if (components[j].isRelative())
                    System.out.print("Relative");
                else
                    System.out.print("Absolute");
                
                if (components[j].isAnalog()) 
                    System.out.print(" Analog");
                else
                    System.out.print(" Digital");
            }
            System.out.println(" ");
            System.out.println(" Digite 9 para o polling do controller ou qualquer outro número para sair");
            choice = scanner.nextInt();
            while (choice == 9) {
                StringBuffer buffer = new StringBuffer();
                for(int i=0;i<components.length;i++) {
                if(i>0) {
                buffer.append(", ");
                }
                buffer.append(components[i].getName());
                buffer.append(": ");
                if(components[i].isAnalog()) {
                    buffer.append(components[i].getPollData());
                } else {
                    if(components[i].getPollData()==1.0f) {
                        buffer.append("On");
                    } else {
                        buffer.append("Off");
                    }
                }
            }
            System.out.println(buffer.toString());

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //choice = scanner.nextInt();
             Thread.sleep(100);
            }
        scanner.close();

    }
    /**
     * Prints all the controllers and its components.
     */
    public void getAllControllersInfo()
    {
        System.out.println("JInput version: " + Version.getVersion());
        System.out.println("");
        
        // Get a list of the controllers JInput knows about and can interact with.
        Controller[] controllersList = ControllerEnvironment.getDefaultEnvironment().getControllers();
        
        // First print all controllers names.
        for(int i =0;i<controllersList.length;i++){
            System.out.println(controllersList[i].getName());
        }

        // Print all components of controllers.
        for(int i = 0; i < controllersList.length; i++){            
            System.out.println("\n");
            System.out.println("-----------------------------------------------------------------");
            
            // Get the name of the controller
            System.out.println(controllersList[i].getName());
            // Get the type of the controller, e.g. GAMEPAD, MOUSE, KEYBOARD, 
            // see http://www.newdawnsoftware.com/resources/jinput/apidocs/net/java/games/input/Controller.Type.html
            System.out.println("Type: "+controllersList[i].getType().toString());

            // Get this controllers components (buttons and axis)
            Component[] components = controllersList[i].getComponents();
            System.out.print("Component count: "+components.length);
            for(int j=0; j<components.length; j++){
                System.out.println("");
                
                // Get the components name
                System.out.println("Component "+j+": "+components[j].getName());
                // Get it's identifier, E.g. BUTTON.PINKIE, AXIS.POV and KEY.Z, 
                // see http://www.newdawnsoftware.com/resources/jinput/apidocs/net/java/games/input/Component.Identifier.html
                System.out.println("    Identifier: "+ components[j].getIdentifier().getName());
                System.out.print("    ComponentType: ");
                if (components[j].isRelative())
                    System.out.print("Relative");
                else
                    System.out.print("Absolute");
                
                if (components[j].isAnalog()) 
                    System.out.print(" Analog");
                else
                    System.out.print(" Digital");
            }
            
            System.out.println("\n");
            System.out.println("-----------------------------------------------------------------");
        }
    }
    
    /**
     * Prints controllers components and its values.
     * 
     * @param controllerType Desired type of the controller.
     */
    public void pollControllerAndItsComponents(Controller.Type controllerType)
    {
        Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
        
        // First controller of the desired type.
        Controller firstController = null;
        
        for(int i=0; i < controllers.length && firstController == null; i++) {
            if(controllers[i].getType() == controllerType) {
                // Found a controller
                firstController = controllers[i];
                break;
            }
        }
        
        if(firstController == null) {
            // Couldn't find a controller
            System.out.println("Found no desired controller!");
            System.exit(0);
        }

        System.out.println("First controller of a desired type is: " + firstController.getName());

        while(true) {
            firstController.poll();
            Component[] components = firstController.getComponents();
            StringBuffer buffer = new StringBuffer();
            for(int i=0;i<components.length;i++) {
                if(i>0) {
                buffer.append(", ");
                }
                buffer.append(components[i].getName());
                buffer.append(": ");
                if(components[i].isAnalog()) {
                    buffer.append(components[i].getPollData());
                } else {
                    if(components[i].getPollData()==1.0f) {
                        buffer.append("On");
                    } else {
                        buffer.append("Off");
                    }
                }
            }
            System.out.println(buffer.toString());

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
}
