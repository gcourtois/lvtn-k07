//
// Generated by JTB 1.3.2
//

package com.res.cobol.syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * relop -> Relop()
 * nodeChoice -> ( ( [ &lt;K_ALL&gt; | &lt;K_ANY&gt; ] &lt;LPARENCHAR&gt; SubQuery() &lt;RPARENCHAR&gt; ) | SQLPriorExpression() | SQLSimpleExpression() )
 * </PRE>
 */
public class SQLRelationalOperatorExpression extends com.res.cobol.RESNode implements Node {
   private Node parent;
   public Relop relop;
   public NodeChoice nodeChoice;

   public SQLRelationalOperatorExpression(Relop n0, NodeChoice n1) {
      relop = n0;
      if ( relop != null ) relop.setParent(this);
      nodeChoice = n1;
      if ( nodeChoice != null ) nodeChoice.setParent(this);
   }

   public SQLRelationalOperatorExpression() {}

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

