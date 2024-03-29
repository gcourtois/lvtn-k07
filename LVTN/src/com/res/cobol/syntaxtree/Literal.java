//
// Generated by JTB 1.3.2
//

package com.res.cobol.syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * nodeOptional -> [ &lt;ALL&gt; ]
 * nodeChoice -> ( NonNumericConstant() | NumericConstant() | FigurativeConstant() | IntrinsicFunction() | SpecialRegister() | &lt;LINAGE_COUNTER&gt; [ ( &lt;IN&gt; | &lt;OF&gt; ) FileName() ] )
 * </PRE>
 */
public class Literal extends com.res.cobol.RESNode implements Node {
   private Node parent;
   public NodeOptional nodeOptional;
   public NodeChoice nodeChoice;

   public Literal(NodeOptional n0, NodeChoice n1) {
      nodeOptional = n0;
      if ( nodeOptional != null ) nodeOptional.setParent(this);
      nodeChoice = n1;
      if ( nodeChoice != null ) nodeChoice.setParent(this);
   }

   public Literal() {}

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

