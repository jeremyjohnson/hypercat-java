package org.openIOT;

public class InvalidItemException  extends RuntimeException {

    public InvalidItemException(){
        super();
    }

    public InvalidItemException(String message){
        super(message);
    }
}