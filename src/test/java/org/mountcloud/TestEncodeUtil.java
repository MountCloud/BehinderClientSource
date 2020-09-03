package org.mountcloud;

import net.rebeyond.behinder.utils.EncodeUtil;
import org.junit.Test;

/**
 * @author zhanghaishan
 * @version V1.0
 * TODO:
 * 2020/9/3.
 */
public class TestEncodeUtil {

    @Test
    public void testMd5(){
        System.out.println(EncodeUtil.md5("zz"));
    }

}
