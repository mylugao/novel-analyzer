package com.unclezs.novel.core.utils.uri;

import com.unclezs.novel.core.utils.StringUtil;
import com.unclezs.novel.core.utils.regex.RegexUtil;

import java.io.UnsupportedEncodingException;

/**
 * @author unclezs.com
 * @date 2019.07.08 19:46
 */
public class UrlEncoder {
    private UrlEncoder() {
    }

    /**
     * 将url里的中文转Unicode
     *
     * @param url     /
     * @param charset /
     * @return /
     */
    public static String encode(String url, String charset) {
        StringBuilder toUrl = new StringBuilder();
        for (char c : url.toCharArray()) {
            String word = String.valueOf(c);
            if (RegexUtil.isChinese(word)) {
                try {
                    toUrl.append(java.net.URLEncoder.encode(word, charset));
                } catch (UnsupportedEncodingException e) {
                    toUrl.append(c);
                    e.printStackTrace();
                }
            } else {
                toUrl.append(c);
            }
        }
        return toUrl.toString();
    }

    /**
     * &#x编码转换成汉字
     *
     * @param src 字符集
     * @return 解码后的字符集
     */
    public static String deCodeUnicode(String src) {
        StringBuilder tmp = new StringBuilder();
        tmp.ensureCapacity(src.length());
        int lastPos = 0;
        int pos;
        char ch;
        src = src.replace("&#x", "%u").replace(";", StringUtil.EMPTY);
        while (lastPos < src.length()) {
            pos = src.indexOf("%", lastPos);
            if (pos == lastPos) {
                if (src.charAt(pos + 1) == 'u') {
                    ch = (char) Integer.parseInt(src.substring(pos + 2, pos + 6), 16);
                    tmp.append(ch);
                    lastPos = pos + 6;
                } else {
                    ch = (char) Integer.parseInt(src.substring(pos + 1, pos + 3), 16);
                    tmp.append(ch);
                    lastPos = pos + 3;
                }
            } else {

                if (pos == -1) {
                    tmp.append(src.substring(lastPos));
                    lastPos = src.length();
                } else {
                    tmp.append(src, lastPos, pos);
                    lastPos = pos;
                }
            }
        }
        return tmp.toString();
    }
}
