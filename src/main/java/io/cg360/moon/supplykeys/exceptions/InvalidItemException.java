package io.cg360.moon.supplykeys.exceptions;

public class InvalidItemException extends RuntimeException {

    public InvalidItemException (ExceptionType type, String item_id, String info) {
        super(getCustomizedMessage(type, item_id, info));
    }

    private static String getCustomizedMessage(ExceptionType type, String item_id, String info){
        String msg = "";
        switch (type){
            case ITEM_ID:
                msg = "Invalid item ID detected in loot pool: "+item_id+" | "+info;
                break;
            case META:
                msg = "Invalid item meta for item detected in loot pool: "+item_id+" | "+info;
        }
        return msg;
    }

    public enum ExceptionType {
        ITEM_ID, META
    }

}
