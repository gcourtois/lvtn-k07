//
// Generated by JTB 1.3.2
//

package com.res.cobol.syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * nodeToken -> &lt;STOP&gt;
 * nodeChoice -> ( &lt;RUN&gt; | Literal() )
 * </PRE>
 */
public class StopStatement extends com.res.cobol.RESNode implements Node {
   private Node parent;
   public NodeToken nodeToken;
   public NodeChoice nodeChoice;

   public StopStatement(NodeToken n0, NodeChoice n1) {
      nodeToken = n0;
      if ( nodeToken != null ) nodeToken.setParent(this);
      nodeChoice = n1;
      if ( nodeChoice != null ) nodeChoice.setParent(this);
   }

   public StopStatement() {}

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

