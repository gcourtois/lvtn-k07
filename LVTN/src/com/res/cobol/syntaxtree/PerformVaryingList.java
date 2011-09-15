//
// Generated by JTB 1.3.2
//

package com.res.cobol.syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * performVarying -> PerformVarying()
 * nodeListOptional -> ( &lt;AFTER&gt; PerformVarying() [ &lt;COMMACHAR&gt; ] )*
 * </PRE>
 */
public class PerformVaryingList extends com.res.cobol.RESNode implements Node {
   private Node parent;
   public PerformVarying performVarying;
   public NodeListOptional nodeListOptional;

   public PerformVaryingList(PerformVarying n0, NodeListOptional n1) {
      performVarying = n0;
      if ( performVarying != null ) performVarying.setParent(this);
      nodeListOptional = n1;
      if ( nodeListOptional != null ) nodeListOptional.setParent(this);
   }

   public PerformVaryingList() {}

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

