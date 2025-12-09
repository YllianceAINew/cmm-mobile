package com.multimediachat.util.datamodel;

import com.multimediachat.global.GlobalConstrants;

/**
 * Created by jack on 12/11/2018.
 */

public class ContactItem {
    public int id;
    public String username;
    public String nickname; //firstname
    public String status; //lastname
    public String prefix;
    public String phone;
    public String city;
    public String birth;
    public String hash;
    public String gender;
    public String region;
    public String email;
    public String userid;
    public String alias;
    public String mobiles;
    public String description;
    public int star;
    public int notsharepost;
    public int hideuserpost;
    public String greeting;
    public int type;
    public int sub_type;

    public ContactItem(int id, String username, String nickname, String status, String prefix, String phone,
                       String city, String birth, String hash, String gender, String region, String email, String userid, String alias, String mobiles, String description,
                       int star, int notsharepost, int hideuserpost, String greeting,
                       int type, int sub_type)
    {
        this.id = id;
        this.username = username;

        if ( username.equals(GlobalConstrants.ADMIN_USERNAME) )
            this.nickname = GlobalConstrants.ADMIN_NICKNAME;
        else
            this.nickname = nickname;

        this.status = status;
        this.prefix = prefix;
        this.phone = phone;
        this.city = city;
        this.birth = birth;
        this.hash = hash;
        this.gender = gender;
        this.region = region;
        this.email = email;
        this.userid = userid;
        this.alias = alias;
        this.mobiles = mobiles;
        this.description = description;
        this.star = star;
        this.notsharepost = notsharepost;
        this.hideuserpost = hideuserpost;
        this.greeting = greeting;
        this.type = type;
        this.sub_type = sub_type;
    }

    public String getDisplayName() {
        if ( alias != null && !alias.isEmpty() )
            return alias;

        return nickname;
    }
}