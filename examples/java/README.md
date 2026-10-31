# Java example

Add the `vn.hitechcloud:vneid-sdk` dependency, then compile with the SDK and Gson on the classpath:

```bash
export VNEID_API_KEY=... VNEID_TARGET_CODE=...
javac -cp "vneid-sdk.jar:gson.jar" BasicUsage.java
java  -cp ".:vneid-sdk.jar:gson.jar" BasicUsage
```
