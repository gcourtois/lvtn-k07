//
// Generated by JTB 1.3.2
//

package com.res.cobol.syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * selectClause -> SelectClause()
 * nodeListOptional -> ( FileControlClause() )*
 * </PRE>
 */
public class FileControlEntry extends com.res.cobol.RESNode implements Node {
   private Node parent;
   public SelectClause selectClause;
   public NodeListOptional nodeListOptional;

   public FileControlEntry(SelectClause n0, NodeListOptional n1) {
      selectClause = n0;
      if ( selectClause != null ) selectClause.setParent(this);
      nodeListOptional = n1;
      if ( nodeListOptional != null ) nodeListOptional.setParent(this);
   }

   public FileControlEntry() {}

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

