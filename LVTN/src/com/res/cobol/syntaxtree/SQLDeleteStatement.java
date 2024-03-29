//
// Generated by JTB 1.3.2
//

package com.res.cobol.syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * nodeToken -> &lt;K_DELETE&gt;
 * nodeOptional -> [ &lt;K_FROM&gt; ]
 * tableReference -> TableReference()
 * nodeOptional1 -> [ RelObjectName() ]
 * nodeOptional2 -> [ &lt;K_WHERE&gt; ( SQLExpression() | &lt;K_CURRENT&gt; &lt;K_OF&gt; RelObjectName() ) ]
 * </PRE>
 */
public class SQLDeleteStatement extends com.res.cobol.RESNode implements Node {
   private Node parent;
   public NodeToken nodeToken;
   public NodeOptional nodeOptional;
   public TableReference tableReference;
   public NodeOptional nodeOptional1;
   public NodeOptional nodeOptional2;

   public SQLDeleteStatement(NodeToken n0, NodeOptional n1, TableReference n2, NodeOptional n3, NodeOptional n4) {
      nodeToken = n0;
      if ( nodeToken != null ) nodeToken.setParent(this);
      nodeOptional = n1;
      if ( nodeOptional != null ) nodeOptional.setParent(this);
      tableReference = n2;
      if ( tableReference != null ) tableReference.setParent(this);
      nodeOptional1 = n3;
      if ( nodeOptional1 != null ) nodeOptional1.setParent(this);
      nodeOptional2 = n4;
      if ( nodeOptional2 != null ) nodeOptional2.setParent(this);
   }

   public SQLDeleteStatement() {}

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

