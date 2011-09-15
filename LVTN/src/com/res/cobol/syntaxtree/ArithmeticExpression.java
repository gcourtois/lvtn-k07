//
// Generated by JTB 1.3.2
//

package com.res.cobol.syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * timesDiv -> TimesDiv()
 * nodeListOptional -> ( ( ( &lt;PLUSCHAR_SUBS&gt; | &lt;PLUSCHAR&gt; ) | ( &lt;MINUSCHAR_SUBS&gt; | &lt;MINUSCHAR&gt; ) ) TimesDiv() )*
 * </PRE>
 */
public class ArithmeticExpression extends com.res.cobol.RESNode implements Node {
   private Node parent;
   public TimesDiv timesDiv;
   public NodeListOptional nodeListOptional;

   public ArithmeticExpression(TimesDiv n0, NodeListOptional n1) {
      timesDiv = n0;
      if ( timesDiv != null ) timesDiv.setParent(this);
      nodeListOptional = n1;
      if ( nodeListOptional != null ) nodeListOptional.setParent(this);
   }

   public ArithmeticExpression() {}

   public void accept(com.res.cobol.visitor.Visitor v) throws Exception {
       v.visit(this);
   }
   public <R,A> R accept(com.res.cobol.visitor.GJVisitor<R,A> v, A argu) throws Exception {
       return v.visit(this,argu);
   }
   public <R> R accept(com.res.cobol.visitor.GJNoArguVisitor<R> v) throws Exception {
       return v.visit(this);
   }
   public <A> void accept(com.res.cobol.visitor.GJVoidVisitor<A> v, A argu) throws Exception {
       v.visit(this,argu);
   }
   public void setParent(Node n) { parent = n; }
   public Node getParent()       { return parent; }
}

