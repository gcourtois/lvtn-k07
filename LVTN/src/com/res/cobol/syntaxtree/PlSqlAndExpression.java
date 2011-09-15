//
// Generated by JTB 1.3.2
//

package com.res.cobol.syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * plSqlUnaryLogicalExpressions -> PlSqlUnaryLogicalExpressions()
 * nodeList -> ( &lt;K_AND&gt; PlSqlUnaryLogicalExpressions() )+
 * </PRE>
 */
public class PlSqlAndExpression extends com.res.cobol.RESNode implements Node {
   private Node parent;
   public PlSqlUnaryLogicalExpressions plSqlUnaryLogicalExpressions;
   public NodeList nodeList;

   public PlSqlAndExpression(PlSqlUnaryLogicalExpressions n0, NodeList n1) {
      plSqlUnaryLogicalExpressions = n0;
      if ( plSqlUnaryLogicalExpressions != null ) plSqlUnaryLogicalExpressions.setParent(this);
      nodeList = n1;
      if ( nodeList != null ) nodeList.setParent(this);
   }

   public PlSqlAndExpression() {}

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

