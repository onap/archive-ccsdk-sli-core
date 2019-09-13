package org.onap.ccsdk.sli.core.api.lang;

public interface SvcLogicAtom extends SvcLogicExpression {

    AtomType getAtomType();

    void setAtomType(String newType);

    String getAtom();

    void setAtomType(AtomType atomType);

    void setAtom(String atom);

    String asParsedExpr();

}
