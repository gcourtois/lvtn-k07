//
// Generated by JTB 1.3.2
//

package com.res.cobol.syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * plSqlMultiplicativeExpressions -> PlSqlMultiplicativeExpressions()
 * nodeList -> ( ( ( &lt;PLUSCHAR&gt; | &lt;PLUSCHAR_SUBS&gt; ) | ( &lt;MINUSCHAR&gt; | &lt;MINUSCHAR_SUBS&gt; ) | "||" ) PlSqlMultiplicativeExpressions() )+
 * </PRE>
 */
public class PlSqlAdditiveExpression extends com.res.cobol.RESNode implements Node {
   private Node parent;
   public PlSqlMultiplicativeExpressions plSqlMultiplicativeExpressions;
   public NodeList nodeList;

   public PlSqlAdditiveExpression(PlSqlMultiplicativeExpressions n0, NodeList n1) {
      plSqlMultiplicativeExpressions = n0;
      if ( plSqlMultiplicativeExpressions != null ) plSqlMultiplicativeExpressions.setParent(this);
      nodeList = n1;
      if ( nodeList != null ) nodeList.setParent(this);
   }

   public PlSqlAdditiveExpression() {}

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

