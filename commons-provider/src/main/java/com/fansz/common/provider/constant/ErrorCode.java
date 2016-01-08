package com.fansz.common.provider.constant;

/**
 * Created by allan on 15/12/23.
 */
public enum ErrorCode {

    SUCCESS("0", "Success"),

    FAIL("1", "Fail"),

    SYSTEM_ERROR("10001", "System error"),//系统错误

    PARAMETERS_ERROR("10008", "Parameter is wrong"),//参数错误

    VERIFY_ERROR("20000", "Verify code wrong"),//验证码错误

    USER_NOT_FOUND("20001", "User not found"),//用户不存在

    MOBILE_NOT_FOUND("20002", "Mobile not found"),//手机号码不存在

    MOBILE_IS_USED("20003", "Mobile is registered "),//手机号码已经被使用

    PASSWORD_WRONG("20004", "Password wrong"),//密码错误

    USER_EXISTS("20005", "User exists"),//用户已存在

    INTERVAL_IS_TOO_SHORT("20006", "Interval is too short"),//发送间隔过短

    ACCESS_TOKEN_INVALID("20007", "AccessToken invalid"),//Access Token无效

    REFRESH_TOKEN_INVALID("20008", "RefreshToken invalid"),//Refresh Token无效

    RELATION_FRIEND_NO_EXISTS("20200", "Cann't add friend"),//无法添加好友

    RELATION_IS_FRIEND("20201", "Already is friend"),//已经是好友

    RELATION_SPECIAL_NO_ADD("20202", "Cann't be special friend"),//无法添加特殊好友

    RELATION_SPECIAL_NO_DEL("20203", "Cann't remove special focus"),//无法取消特殊好友

    RELATION_IS_NOT_FRIEND("20204", "Not friend"),//不是好友,无法添加特殊好友

    RELATION_IS_IN_FANDOM("20205", "User already in fandom"),//用户已经关注fandom

    RELATION_IS_NOT_IN_FANDOM("20206", "Use not in fandom"),//用户未关注fandom

    RELATION_IS_SPACIEL_FANDOM("20207", "User already special focus fandom"),//用户已经特殊关注fandom

    RELATION_IS_NOT_SPACIEL_FANDOM("20208", "User not special focus fandom"),//用户未特殊关注fandom

    POST_NOT_EXISTS("20301", "Post not exits"),//POST不存在

    POST_NOT_ALLOW_DEL("20302", "Cann't delete other's post"),//无法删除他人帖子

    LIKED_NO_DELETE("20303", "No authoriy to delete like"),//无删除点赞权限

    LIKED_REPEATED("20304", "Liked already"),//重复点赞

    NICK_NAME_REPATEDD("20401", "Nickname not unique"),//nickname重复

    FANDOM_NAME_REPATEDD("20402", "Fandom name not unique"),//fandom名称重复

    FANDOM_NO_DELETE("20403", "No authoriy to delete fandom"), //没有删除fandom权限

    FANDOM_MONDIFY_NOT_PERMISSION("20404", "No authoriy to modify fandom"), //没有权限修改fandom

    TAG_NAME_NOT_UNIQUE("20405", "Tag name must be unique"), //TAG名称不唯一

    TAG_NOT_FOUND("20406", "Tag not exist"), //TAG不存在

    COMMENT_NO_AUTHORITY_DELETE("20501", "No authority to delete comment");//无权限删除评论


    private String code;
    private String name;

    private ErrorCode(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }

    public static ErrorCode getTypeByCode(String code) {
        ErrorCode[] codes = values();
        int len = codes.length;

        for (int i = 0; i < len; ++i) {
            ErrorCode errorCode = codes[i];
            if (errorCode.getCode().equals(code)) {
                return errorCode;
            }
        }

        return null;
    }

}
