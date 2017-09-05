package com.babyfs.tk.commons.validator.rule;

import com.google.common.base.Strings;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * 身份证验证规则
 */
@XmlRootElement(name = "id_card")
public class IdCardRule implements IValidateRule {
    /**
     * 十七位数字本体码权重
     */
    private static final int[] weight = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
    /**
     * mod11,对应校验码字符值
     */
    private static final char[] validate = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

    @Override
    public boolean validate(String value) {
        return Strings.isNullOrEmpty(value) || validateId(value);
    }

    @Override
    public String getErrorMsg() {
        return "not a valid id card";
    }

    private static boolean validateId(String id) {
        if (id.length() != 18) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 17; i++) {
            int c = id.charAt(i) - '0';
            if (c < 0 || c > 9) {
                return false;
            }
            sum = sum + c * weight[i];
        }
        int mode = sum % 11;
        char c = validate[mode];
        char vc = id.charAt(17);
        if (vc == 'x') {
            vc = 'X';
        }
        return c == vc;
    }
}
