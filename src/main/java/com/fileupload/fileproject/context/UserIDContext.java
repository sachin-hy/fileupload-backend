package com.fileupload.fileproject.context;

public class UserIDContext {

    private static final ThreadLocal<Long> userId = new ThreadLocal<>();

    public static void setContext(Long id){
        userId.set(id);
    }

    public static Long getUserId(){

        return userId.get();
    }

    public static void clear(){
        userId.remove();
    }
}
