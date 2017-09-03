package com.github.animalize.ting.TTS;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TTSUtils {
    private static final int SIZE = 64;//256;

    private static Pattern biaodian;


    public static List<Ju> fenJu(String s) {
        if (biaodian == null) {
            biaodian = Pattern.compile("^.*[\n，。！？；：,.!?]", Pattern.DOTALL);
        }

        List<Ju> ret = new ArrayList<>();

        int p = 0;

        while (true) {
            if (p + SIZE >= s.length()) {
                Ju ju = new Ju(p, s.length());
                ret.add(ju);

                break;
            }

            String sub = s.substring(p, p + SIZE);
            Matcher m = biaodian.matcher(sub);

            int end;
            if (m.find()) {
                end = m.end();
            } else {
                final char last = sub.charAt(sub.length() - 1);
                if (0xD800 <= last && last <= 0xDBFF) {
                    // last char is high surrogate
                    end = sub.length() - 1;
                } else {
                    end = sub.length();
                }
            }

            Ju ju = new Ju(p, p + end);
            ret.add(ju);
            p += end;
        }

        return ret;
    }
}
