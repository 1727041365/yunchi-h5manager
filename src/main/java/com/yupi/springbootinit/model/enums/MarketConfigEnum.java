package com.yupi.springbootinit.model.enums;

import java.util.Random;

public enum MarketConfigEnum {
//    UID("12422457"),
//    TOKEN("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiIiLCJhdWQiOiIiLCJqdGkiOiIxMjQyMjQ1NyIsImlhdCI6MTc1MjExNTU1MCwibmJmIjoxNzUyMTE1NTUwLCJleHAiOjE3NTM5NTI5MjMsInR5cGUiOiIyMDk3NV92MTFhcHAiLCJhdXRoZW50aWNhdGlvblN0YXRlIjpmYWxzZX0.aVoPWd1th4W27pG1dFcugvFLngZ8zEkyRoy9JldZ-1I"),

    UID("14323289"),
    TOKEN("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiIiLCJhdWQiOiIiLCJqdGkiOiIxNDMyMzI4OSIsImlhdCI6MTc1MzU4Mzc1OCwibmJmIjoxNzUzNTgzNzU4LCJleHAiOjE3NTYxNzU3NTgsInR5cGUiOiJ2MTFhcHAiLCJhdXRoZW50aWNhdGlvblN0YXRlIjp0cnVlfQ.mGmEcaQLAdXXk-qxbx08SA-djshovgc8MgBxV7vjAI8"),
    CJBUID("14323289"),
    CJBTOKEN("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiIiLCJhdWQiOiIiLCJqdGkiOiIxNDMyMzI4OSIsImlhdCI6MTc1MzU4Mzc1OCwibmJmIjoxNzUzNTgzNzU4LCJleHAiOjE3NTYxNzU3NTgsInR5cGUiOiJ2MTFhcHAiLCJhdXRoZW50aWNhdGlvblN0YXRlIjp0cnVlfQ.mGmEcaQLAdXXk-qxbx08SA-djshovgc8MgBxV7vjAI8"),
    KEY("BHbE9oCgl58NUz5oJVDUFMLJO9vGQnvdv0Lem3315wQG8laB4dGcxIXFLfDsInHTa"),
    ANDROID_ID(generateRandomAndroidId()),
    StoenUID("14323289"),
    StoneTOKEN("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiIiLCJhdWQiOiIiLCJqdGkiOiIxNDMyMzI4OSIsImlhdCI6MTc1MzA3Mjk3NiwibmJmIjoxNzUzMDcyOTc2LCJleHAiOjE3NTU2NjQ5NzYsInR5cGUiOiJ2OGFwcCIsImF1dGhlbnRpY2F0aW9uU3RhdGUiOnRydWV9.2RRkHSh5lJ-tESQ1J1zT8qpQNVk5thk43wmdNIgDIK4");
    private final String value;

    MarketConfigEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
    // 静态方法：生成随机AndroidId（16位十六进制字符串）
    private static String generateRandomAndroidId() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        String chars = "0123456789abcdef"; // 十六进制字符集

        for (int i = 0; i < 16; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
