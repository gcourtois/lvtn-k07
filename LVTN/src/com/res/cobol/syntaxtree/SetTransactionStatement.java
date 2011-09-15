//
// Generated by JTB 1.3.2
//

package com.res.cobol.syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * nodeToken -> &lt;K_SET&gt;
 * nodeToken1 -> &lt;K_TRANSACTION&gt;
 * nodeChoice -> ( ( &lt;K_READ&gt; ( &lt;K_ONLY&gt; | &lt;K_WRITE&gt; ) ) | ( &lt;K_USE&gt; &lt;K_ROLLBACK&gt; &lt;K_SEGMENT&gt; RelObjectName() ) )
 * </PRE>
 */
public class SetTransactionStatement extends com.res.cobol.RESNode implements Node {
   private Node parent;
   public NodeToken nodeToken;
   public NodeToken nodeToken1;
   public NodeChoice nodeChoice;

   public SetTransactionStatement(NodeToken n0, NodeToken n1, NodeChoice n2) {
      nodeToken = n0;
      if ( nodeToken != null ) nodeToken.setParent(this);
      nodeToken1 = n1;
      if ( nodeToken1 != null ) nodeToken1.setParent(this);
      nodeChoice = n2;
      if ( nodeChoice != null ) nodeChoice.setParent(this);
   }

   public SetTransactionStatement() {}

   public void accept(com.res.cobol.visitor.Visitor v) throws Exception {
       v.visit(this);
   }

   public <R, A> R accept(com.res.cobol.visitor.GJVisitor<R, A> v, A argu)
           throws Exception {
       return v.visit(this, argu);
   }

   public <R> R accept(com.res.cobol.visitor.GJNoArguVisitor<R> v)
           throws Exception {
       return v.visit(this);
   }

   public <A> void accept(com.res.cobol.visitor.GJVoidVisitor<A> v, A argu)
           throws Exception {
       v.visit(this, argu);
   }
   public void setParent(Node n) { parent = n; }
   public Node getParent()       { return parent; }
}

