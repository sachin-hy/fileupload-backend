package com.fileupload.fileproject.context;

public class LookupContext {

    private static final ThreadLocal<String> subdomainHolder = new ThreadLocal<>();
   // private static final ThreadLocal<String> emailHolder = new ThreadLocal<>();

    public static void setContext(String subdomain){
        subdomainHolder.set(subdomain);
       // emailHolder.set(email);
    }

    public static String getSubdomain(){
        return subdomainHolder.get();
    }

//    public static String getEmail(){
//        return emailHolder.get();
//    }

    public static void clear(){
        subdomainHolder.remove();
        // emailHolder.remove();
    }
}
