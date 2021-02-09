package com.example.histoquiz.util;

import java.util.Random;

public class RoomCreator {
    protected String upperAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    protected String lowerAlphabet = "abcdefghijklmnopqrstuvwxyz";
    protected String numbers = "0123456789";
    protected String alphaNumeric = upperAlphabet + lowerAlphabet + numbers;
    protected StringBuilder sb = new StringBuilder();
    protected Random random = new Random();
    protected String actualRoomName;

    public String newRoomCode(int length){
        for(int i = 0; i < length; i++) {
            int index = random.nextInt(alphaNumeric.length());
            char randomChar = alphaNumeric.charAt(index);
            sb.append(randomChar);
        }
        actualRoomName = sb.toString();
        return actualRoomName;
    }

    public String getActualRoomName(){
        return actualRoomName;
    }

    public void setActualRoomName(String roomName){
        this.actualRoomName = roomName;
    }
}
