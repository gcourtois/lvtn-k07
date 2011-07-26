//
// Generated by JTB 1.3.2
//

package com.res.cobol.syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * idOrLiteral -> IdOrLiteral()
 * nodeToken -> &lt;BY&gt;
 * nodeChoice -> ( IdOrLiteral() &lt;GIVING&gt; ArithIdentifierList() | ArithIdentifierList() )
 * </PRE>
 */
public class MultiplyBody extends com.res.cobol.RESNode implements Node {
   private Node parent;
   public IdOrLiteral idOrLiteral;
   public NodeToken nodeToken;
   public NodeChoice nodeChoice;

   public MultiplyBody(IdOrLiteral n0, NodeToken n1, NodeChoice n2) {
      idOrLiteral = n0;
      if ( idOrLiteral != null ) idOrLiteral.setParent(this);
      nodeToken = n1;
      if ( nodeToken != null ) nodeToken.setParent(this);
      nodeChoice = n2;
      if ( nodeChoice != null ) nodeChoice.setParent(this);
   }

   public MultiplyBody() {}

   public void accept(com.res.cobol.visitor.Visitor v) {
      v.visit(this);
   }
   public <R,A> R accept(com.res.cobol.visitor.GJVisitor<R,A> v, A argu) {
      return v.visit(this,argu);
   }
   public <R> R accept(com.res.cobol.visitor.GJNoArguVisitor<R> v) {
      return v.visit(this);
   }
   public <A> void accept(com.res.cobol.visitor.GJVoidVisitor<A> v, A argu) {
      v.visit(this,argu);
   }
   public void setParent(Node n) { parent = n; }
   public Node getParent()       { return parent; }
}

