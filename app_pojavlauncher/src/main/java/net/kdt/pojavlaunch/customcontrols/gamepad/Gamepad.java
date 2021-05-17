package net.kdt.pojavlaunch.customcontrols.gamepad;

import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;

import net.kdt.pojavlaunch.BaseMainActivity;
import net.kdt.pojavlaunch.LWJGLGLFWKeycode;

import org.lwjgl.glfw.CallbackBridge;

import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.DIRECTION_EAST;
import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.DIRECTION_NONE;
import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.DIRECTION_NORTH;
import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.DIRECTION_NORTH_EAST;
import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.DIRECTION_NORTH_WEST;
import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.DIRECTION_SOUTH;
import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.DIRECTION_SOUTH_EAST;
import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.DIRECTION_SOUTH_WEST;
import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.DIRECTION_WEST;
import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.JOYSTICK_DEADZONE;

public class Gamepad {

    private BaseMainActivity gameActivity;


    private GamepadDpad gamepadDpad = new GamepadDpad();

    private final GamepadJoystick leftJoystick = new GamepadJoystick(MotionEvent.AXIS_X, MotionEvent.AXIS_Y);
    private int currentJoystickDirection = DIRECTION_NONE;

    private final GamepadJoystick rightJoystick = new GamepadJoystick(MotionEvent.AXIS_Z, MotionEvent.AXIS_RZ);
    private float lastHorizontalValue = 0.0f;
    private float lastVerticalValue = 0.0f;

    private final double mouseMaxAcceleration = 2f;
    private double acceleration = 0.0f;

    private double mouseMagnitude;
    private double mouseAngle;
    private double mouseSensitivity = 19;

    private final GamepadMapping gameMap = new GamepadMapping();
    private final GamepadMapping menuMap = new GamepadMapping();
    private GamepadMapping currentMap = menuMap;

    private boolean leftThumbstickToggle = false;
    private boolean rightThumbstickToggle = false;

    private boolean isGrabbing = false;


    private Thread mouseThread;

    public Gamepad(BaseMainActivity gameActivity){
        this.gameActivity = gameActivity;
        createMapping();

        mouseThread = new Thread("Gamepad Thread"){
            long lastTime = System.nanoTime();
            final double ticks = 60D;
            final double ns = 1000000000 / ticks;
            double delta = 0;

            @Override
            public void run() {


                while (!isInterrupted()) {
                    long now = System.nanoTime();
                    delta += (now - lastTime) / ns;
                    lastTime = now;
                    if(delta >= 1) {

                        updateGrabbingState();

                        tick();

                        delta--;
                        try {
                            sleep((long) ((1 - delta)/ticks));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }



            private void tick(){
                if(lastHorizontalValue != 0 || lastVerticalValue != 0){

                    acceleration = (mouseMagnitude - JOYSTICK_DEADZONE)/(1 - JOYSTICK_DEADZONE);
                    acceleration = Math.pow(acceleration, mouseMaxAcceleration);

                    if(acceleration > 1){
                        acceleration = 1;
                    }

                    gameActivity.mouse_x += Math.cos(mouseAngle) * acceleration * mouseSensitivity;
                    gameActivity.mouse_y -= Math.sin(mouseAngle) * acceleration * mouseSensitivity;

                    CallbackBridge.sendCursorPos(gameActivity.mouse_x, gameActivity.mouse_y);
                    if(!isGrabbing){
                        gameActivity.placeMouseAt(gameActivity.mouse_x / gameActivity.scaleFactor, gameActivity.mouse_y  / gameActivity.scaleFactor);
                    }
                }

            }
        };
        mouseThread.setPriority(1);
        mouseThread.start();
    }

    private void updateGrabbingState() {
        boolean lastGrabbingValue = isGrabbing;
        isGrabbing = CallbackBridge.isGrabbing();
        if(lastGrabbingValue != isGrabbing){
            if(isGrabbing){
                //TODO hide the cursor
                currentMap = gameMap;
            }else{
                //TODO place the cursor at the center
                currentMap = menuMap;

                gameActivity.mouse_x = CallbackBridge.windowWidth/2;
                gameActivity.mouse_y = CallbackBridge.windowHeight/2;
                CallbackBridge.sendCursorPos(gameActivity.mouse_x, gameActivity.mouse_y);
                gameActivity.placeMouseAt(CallbackBridge.physicalWidth/2, CallbackBridge.physicalHeight/2);
            }

        }

    }

    private void createMapping(){
        //create mappings to fit our needs

        //GAMEMAP
        gameMap.BUTTON_A = new int[]{LWJGLGLFWKeycode.GLFW_KEY_SPACE};
        gameMap.BUTTON_B = new int[]{LWJGLGLFWKeycode.GLFW_KEY_Q};
        gameMap.BUTTON_X = new int[]{LWJGLGLFWKeycode.GLFW_KEY_F};
        gameMap.BUTTON_Y = new int[]{LWJGLGLFWKeycode.GLFW_KEY_E};

        gameMap.DIRECTION_FORWARD = new int[]{LWJGLGLFWKeycode.GLFW_KEY_W};
        gameMap.DIRECTION_BACKWARD = new int[]{LWJGLGLFWKeycode.GLFW_KEY_S};
        gameMap.DIRECTION_RIGHT = new int[]{LWJGLGLFWKeycode.GLFW_KEY_D};
        gameMap.DIRECTION_LEFT = new int[]{LWJGLGLFWKeycode.GLFW_KEY_A};

        gameMap.DPAD_UP = new int[]{LWJGLGLFWKeycode.GLFW_KEY_UNKNOWN};
        gameMap.DPAD_DOWN = new int[]{LWJGLGLFWKeycode.GLFW_KEY_UNKNOWN};
        gameMap.DPAD_RIGHT = new int[]{LWJGLGLFWKeycode.GLFW_KEY_UNKNOWN};
        gameMap.DPAD_LEFT = new int[]{LWJGLGLFWKeycode.GLFW_KEY_UNKNOWN};

        gameMap.SHOULDER_LEFT = new int[]{GamepadMapping.MOUSE_SCROLL_UP};
        gameMap.SHOULDER_RIGHT = new int[]{GamepadMapping.MOUSE_SCROLL_DOWN};

        gameMap.TRIGGER_LEFT = new int[]{LWJGLGLFWKeycode.GLFW_MOUSE_BUTTON_RIGHT};
        gameMap.TRIGGER_RIGHT = new int[]{LWJGLGLFWKeycode.GLFW_MOUSE_BUTTON_LEFT};

        gameMap.THUMBSTICK_LEFT = new int[]{LWJGLGLFWKeycode.GLFW_KEY_LEFT_SHIFT};
        gameMap.THUMBSTICK_RIGHT = new int[]{LWJGLGLFWKeycode.GLFW_KEY_F5};

        gameMap.BUTTON_START = new int[]{LWJGLGLFWKeycode.GLFW_KEY_ESCAPE};
        gameMap.BUTTON_SELECT = new int[]{LWJGLGLFWKeycode.GLFW_KEY_UNKNOWN};


        //MENU MAP
        menuMap.BUTTON_A = new int[]{LWJGLGLFWKeycode.GLFW_MOUSE_BUTTON_LEFT};
        menuMap.BUTTON_B = new int[]{LWJGLGLFWKeycode.GLFW_KEY_ESCAPE};
        menuMap.BUTTON_X = new int[]{LWJGLGLFWKeycode.GLFW_MOUSE_BUTTON_RIGHT};
        menuMap.BUTTON_Y = new int[]{LWJGLGLFWKeycode.GLFW_KEY_LEFT_SHIFT, LWJGLGLFWKeycode.GLFW_MOUSE_BUTTON_RIGHT}; //Oops, doesn't work since left shift isn't properly applied.

        menuMap.DIRECTION_FORWARD = new int[]{GamepadMapping.MOUSE_SCROLL_UP, GamepadMapping.MOUSE_SCROLL_UP, GamepadMapping.MOUSE_SCROLL_UP,GamepadMapping.MOUSE_SCROLL_UP,GamepadMapping.MOUSE_SCROLL_UP};
        menuMap.DIRECTION_BACKWARD = new int[]{GamepadMapping.MOUSE_SCROLL_DOWN, GamepadMapping.MOUSE_SCROLL_DOWN, GamepadMapping.MOUSE_SCROLL_DOWN,GamepadMapping.MOUSE_SCROLL_DOWN,GamepadMapping.MOUSE_SCROLL_DOWN};
        menuMap.DIRECTION_RIGHT = new int[]{LWJGLGLFWKeycode.GLFW_KEY_UNKNOWN};
        menuMap.DIRECTION_LEFT = new int[]{LWJGLGLFWKeycode.GLFW_KEY_UNKNOWN};

        menuMap.DPAD_UP = new int[]{LWJGLGLFWKeycode.GLFW_KEY_UNKNOWN};
        menuMap.DPAD_DOWN = new int[]{LWJGLGLFWKeycode.GLFW_KEY_UNKNOWN};
        menuMap.DPAD_RIGHT = new int[]{LWJGLGLFWKeycode.GLFW_KEY_UNKNOWN};
        menuMap.DPAD_LEFT = new int[]{LWJGLGLFWKeycode.GLFW_KEY_UNKNOWN};

        menuMap.SHOULDER_LEFT = new int[]{GamepadMapping.MOUSE_SCROLL_UP};
        menuMap.SHOULDER_RIGHT = new int[]{GamepadMapping.MOUSE_SCROLL_DOWN};

        menuMap.TRIGGER_LEFT = new int[]{LWJGLGLFWKeycode.GLFW_KEY_UNKNOWN};
        menuMap.TRIGGER_RIGHT = new int[]{LWJGLGLFWKeycode.GLFW_KEY_UNKNOWN};

        menuMap.THUMBSTICK_LEFT = new int[]{LWJGLGLFWKeycode.GLFW_KEY_UNKNOWN};
        menuMap.THUMBSTICK_RIGHT = new int[]{LWJGLGLFWKeycode.GLFW_KEY_UNKNOWN};

        menuMap.BUTTON_START = new int[]{LWJGLGLFWKeycode.GLFW_KEY_ESCAPE};
        menuMap.BUTTON_SELECT = new int[]{LWJGLGLFWKeycode.GLFW_KEY_UNKNOWN};


    }


    public void update(InputEvent event){
        if(event instanceof MotionEvent){
            update((MotionEvent) event);
        }
        if(event instanceof KeyEvent){
            update((KeyEvent) event);
        }
    }

    private void update(KeyEvent event){
        sendButton(event.getKeyCode(), event.getAction() == KeyEvent.ACTION_DOWN, getCurrentMap());
    }

    private void update(MotionEvent event){
        updateDirectionalJoystick(event);
        updateMouseJoystick(event);
    }

    private void updateMouseJoystick(MotionEvent event){
        GamepadJoystick currentJoystick = CallbackBridge.isGrabbing() ? rightJoystick : leftJoystick;

        lastHorizontalValue = currentJoystick.getHorizontalAxis(event);
        lastVerticalValue = currentJoystick.getVerticalAxis(event);

        mouseMagnitude = currentJoystick.getMagnitude(event);
        mouseAngle = currentJoystick.getAngleRadian(event);
    }

    private void updateDirectionalJoystick(MotionEvent event){
        GamepadJoystick currentJoystick = CallbackBridge.isGrabbing() ? leftJoystick : rightJoystick;

        int lastJoystickDirection = currentJoystickDirection;
        currentJoystickDirection = currentJoystick.getHeightDirection(event);

        if(currentJoystickDirection != lastJoystickDirection){
            sendDirectionalKeycode(lastJoystickDirection, false, getCurrentMap());
            sendDirectionalKeycode(currentJoystickDirection, true, getCurrentMap());
        }
    }

    private void updateAnalogTriggers(MotionEvent event){
        sendInput(getCurrentMap().TRIGGER_RIGHT, event.getAxisValue(MotionEvent.AXIS_RTRIGGER) > 0.5);
        sendInput(getCurrentMap().TRIGGER_LEFT, event.getAxisValue(MotionEvent.AXIS_LTRIGGER) > 0.5);

    }

    private GamepadMapping getCurrentMap(){
        return currentMap;
    }

    private static void sendDirectionalKeycode(int direction, boolean isDown, GamepadMapping map){
        switch (direction){
            case DIRECTION_NORTH:
                sendInput(map.DIRECTION_FORWARD, isDown);
                break;
            case DIRECTION_NORTH_EAST:
                sendInput(map.DIRECTION_FORWARD, isDown);
                sendInput(map.DIRECTION_RIGHT, isDown);
                break;
            case DIRECTION_EAST:
                sendInput(map.DIRECTION_RIGHT, isDown);
                break;
            case DIRECTION_SOUTH_EAST:
                sendInput(map.DIRECTION_RIGHT, isDown);
                sendInput(map.DIRECTION_BACKWARD, isDown);
                break;
            case DIRECTION_SOUTH:
                sendInput(map.DIRECTION_BACKWARD, isDown);
                break;
            case DIRECTION_SOUTH_WEST:
                sendInput(map.DIRECTION_BACKWARD, isDown);
                sendInput(map.DIRECTION_LEFT, isDown);
                break;
            case DIRECTION_WEST:
                sendInput(map.DIRECTION_LEFT, isDown);
                break;
            case DIRECTION_NORTH_WEST:
                sendInput(map.DIRECTION_FORWARD, isDown);
                sendInput(map.DIRECTION_LEFT, isDown);
                break;
        }
    }


    private void sendButton(int keycode, boolean isDown, GamepadMapping map){
        switch (keycode){
            case KeyEvent.KEYCODE_BUTTON_A:
                sendInput(map.BUTTON_A, isDown);
                break;
            case KeyEvent.KEYCODE_BUTTON_B:
                sendInput(map.BUTTON_B, isDown);
                break;
            case KeyEvent.KEYCODE_BUTTON_X:
                sendInput(map.BUTTON_X, isDown);
                break;
            case KeyEvent.KEYCODE_BUTTON_Y:
                sendInput(map.BUTTON_Y, isDown);
                break;

                //Shoulders
            case KeyEvent.KEYCODE_BUTTON_L1:
                sendInput(map.SHOULDER_LEFT, isDown);
                break;
            case KeyEvent.KEYCODE_BUTTON_R1:
                sendInput(map.SHOULDER_RIGHT, isDown);
                break;

                //Triggers
            case KeyEvent.KEYCODE_BUTTON_L2:
                sendInput(map.TRIGGER_LEFT, isDown);
                break;
            case KeyEvent.KEYCODE_BUTTON_R2:
                sendInput(map.TRIGGER_RIGHT, isDown);
                break;

                //L3 || R3
            case KeyEvent.KEYCODE_BUTTON_THUMBL:
                leftThumbstickToggle = !leftThumbstickToggle;
                if(leftThumbstickToggle) sendInput(map.THUMBSTICK_LEFT, isDown);
                break;
            case KeyEvent.KEYCODE_BUTTON_THUMBR:
                rightThumbstickToggle = !rightThumbstickToggle;
                if(rightThumbstickToggle) sendInput(map.THUMBSTICK_RIGHT, isDown);
                break;

            case KeyEvent.KEYCODE_BUTTON_START:
                sendInput(map.BUTTON_START, isDown);
                break;
            case KeyEvent.KEYCODE_BUTTON_SELECT:
                sendInput(map.BUTTON_SELECT, isDown);
                break;


            default:
                BaseMainActivity.sendKeyPress(LWJGLGLFWKeycode.GLFW_KEY_SPACE, CallbackBridge.getCurrentMods(), isDown);
                break;
        }
    }

    private static void sendInput(int[] keycodes, boolean isDown){
        for(int keycode : keycodes){
            switch (keycode){
                case GamepadMapping.MOUSE_SCROLL_DOWN:
                    if(isDown) CallbackBridge.sendScroll(0, -1);
                    return;
                case GamepadMapping.MOUSE_SCROLL_UP:
                    if(isDown) CallbackBridge.sendScroll(0, 1);
                    return;

                case LWJGLGLFWKeycode.GLFW_MOUSE_BUTTON_RIGHT:
                    BaseMainActivity.sendMouseButton(LWJGLGLFWKeycode.GLFW_MOUSE_BUTTON_RIGHT, isDown);
                    return;
                case LWJGLGLFWKeycode.GLFW_MOUSE_BUTTON_LEFT:
                    BaseMainActivity.sendMouseButton(LWJGLGLFWKeycode.GLFW_MOUSE_BUTTON_LEFT, isDown);
                    return;


                default:
                    BaseMainActivity.sendKeyPress(keycode, CallbackBridge.getCurrentMods(), isDown);
                    return;
            }
        }

    }

    public static boolean isGamepadEvent(InputEvent event){
        if(event instanceof KeyEvent){
            return (event.getSource() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD;
        }
        if(event instanceof MotionEvent){
            return GamepadJoystick.isJoystickEvent((MotionEvent) event) || GamepadDpad.isDpadEvent(event);
        }

        return false;
    }

}
