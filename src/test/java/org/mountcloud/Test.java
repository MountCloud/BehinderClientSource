package org.mountcloud;

/**
 * Behinder
 * org.mountcloud
 * DESC
 * @date 2021/4/11
 */
public class Test {
    @org.junit.Test
    public void test(){
        String var1 = "injector.dll";
        String osArch = System.getProperty("os.arch");
        if (osArch.contains("64")) {
            var1 = "injector64.dll";
        }

        System.out.println(var1);
    }
}
