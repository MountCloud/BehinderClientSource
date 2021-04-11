package net.rebeyond.behinder.utils;

/**
 * Behinder
 * net.rebeyond.behinder.utils
 * DESC
 *
 * @author 张海山 <zhanghaishan@360.cn>
 * @date 2021/4/11
 */
public class StringUtils {

    public static final String EMPTY = "";

    public static String toString(Object obj){
        if(obj==null){
            return null;
        }
        return obj.toString();
    }

    public static String toStringEmpty(Object obj){
        if(obj==null){
            return EMPTY;
        }
        return obj.toString();
    }
}
