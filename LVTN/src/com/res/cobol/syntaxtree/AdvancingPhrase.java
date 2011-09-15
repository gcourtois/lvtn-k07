//
// Generated by JTB 1.3.2
//

package com.res.cobol.syntaxtree;

import com.res.cobol.visitor.GJNoArguVisitor;
import com.res.cobol.visitor.GJVisitor;
import com.res.cobol.visitor.GJVoidVisitor;
import com.res.cobol.visitor.Visitor;

/**
 * Grammar production:
 * 
 * <PRE>
 * nodeChoice -&gt; ( &lt;BEFORE&gt; | &lt;AFTER&gt; )
 * nodeOptional -&gt; [ &lt;ADVANCING&gt; ]
 * nodeChoice1 -&gt; ( &lt;PAGE&gt; | ( Identifier() | IntegerConstant() | FigurativeConstant() ) [ ( &lt;LINE&gt; | &lt;LINES&gt; ) ] | MnemonicName() )
 * </PRE>
 */
public class AdvancingPhrase extends com.res.cobol.RESNode implements Node {
    private Node parent;
    public NodeChoice nodeChoice;
    public NodeOptional nodeOptional;
    public NodeChoice nodeChoice1;

    public AdvancingPhrase(NodeChoice n0, NodeOptional n1, NodeChoice n2) {
        nodeChoice = n0;
        if (nodeChoice != null)
            nodeChoice.setParent(this);
        nodeOptional = n1;
        if (nodeOptional != null)
            nodeOptional.setParent(this);
        nodeChoice1 = n2;
        if (nodeChoice1 != null)
            nodeChoice1.setParent(this);
    }

    public AdvancingPhrase() {
    }

    public void setParent(Node n) {
        parent = n;
    }

    public Node getParent() {
        return parent;
    }

    @Override
    public void accept(Visitor v) throws Exception {
        v.visit(this);
    }

    @Override
    public <R, A> R accept(GJVisitor<R, A> v, A argu) throws Exception {
        return v.visit(this, argu);
    }

    @Override
    public <R> R accept(GJNoArguVisitor<R> v) throws Exception {
        return v.visit(this);
    }

    @Override
    public <A> void accept(GJVoidVisitor<A> v, A argu) throws Exception {
        v.visit(this, argu);
    }
}
