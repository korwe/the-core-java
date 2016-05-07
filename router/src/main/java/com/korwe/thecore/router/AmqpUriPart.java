package com.korwe.thecore.router;

/**
 * @author <a href="mailto:nithia.govender@korwe.com">Nithia Govender</a>
 */
public enum AmqpUriPart {
    Options("exchangePattern=InOnly");

    private final String value;

    AmqpUriPart(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
