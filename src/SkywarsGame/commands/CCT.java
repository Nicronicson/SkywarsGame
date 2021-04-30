package SkywarsGame.commands;

public class CCT {
    public String[] removeCommand(String[] strings){
        if(strings.length > 1){
            String[] newStrings = new String[strings.length-1];
            System.arraycopy(strings, 1, newStrings, 0, newStrings.length);
            return newStrings;
        }
        return new String[0];
    }
}
