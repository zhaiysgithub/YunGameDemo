package kptech.game.kit;

import com.yd.yunapp.gameboxlib.MemberLevel;


public enum UserLevel {
    NORMAL,
    VIP,
    SUPER_VIP;

    private UserLevel() {
    }

    protected static MemberLevel getMemberLevel(UserLevel level){
        switch (level){
            case VIP:
                return MemberLevel.VIP;
            case SUPER_VIP:
                return MemberLevel.SUPER_VIP;
        }
        return MemberLevel.NORMAL;
    }

    public static UserLevel getUserLevel(MemberLevel memberLevel) {
        if (memberLevel == MemberLevel.SUPER_VIP){
            return UserLevel.SUPER_VIP;
        }else if(memberLevel == MemberLevel.VIP){
            return UserLevel.VIP;
        }else {
            return UserLevel.NORMAL;
        }

    }
}