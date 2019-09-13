package org.onap.ccsdk.sli.core.api.lang;

public enum OperatorType {
    addOp("+"),
    subOp("-"),
    multOp("*"),
    divOp("/"),
    equalOp("=="),
    ltOp("<"),
    leOp("<="),
    gtOp(">"),
    geOp(">="),
    neOp("!="),
    andOp("and"),
    orOp("or");

    private String text;

    private OperatorType(String text) {
        this.text = text;
    }

    public String getText() {
        return (text);
    }

    public static OperatorType fromString(String text) {
        if (text != null) {
            for (OperatorType t : OperatorType.values()) {
                if (text.equalsIgnoreCase(t.getText())) {

                    return (t);
                }
            }
        }
        return (null);
    }

    public String toString() {
        return (text);
    }
}
