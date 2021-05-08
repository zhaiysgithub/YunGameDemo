package kptech.game.kit.utils;

public class AppUtils {

    /**
     * 简单校验手机号码
     */
    public static boolean phoneNumSimpleCheck(String phoneNum){

        return phoneNum != null && phoneNum.length() == 11;
    }

    /**
     * 简单校验身份证信息
     */
    public static boolean userIdCardSimpleCheck(String userIdCard){

        return userIdCard != null && userIdCard.length() == 18;
    }


}
