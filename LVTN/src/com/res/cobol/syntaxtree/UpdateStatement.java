//
// Generated by JTB 1.3.2
//

package com.res.cobol.syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * nodeToken -> &lt;K_UPDATE&gt;
 * tableReference -> TableReference()
 * nodeOptional -> [ RelObjectName() ]
 * nodeToken1 -> &lt;K_SET&gt;
 * columnValues -> ColumnValues()
 * nodeOptional1 -> [ &lt;K_WHERE&gt; ( SQLExpression() | &lt;K_CURRENT&gt; &lt;K_OF&gt; RelObjectName() ) ]
 * </PRE>
 */
public class UpdateStatement extends com.res.cobol.RESNode implements Node {
   private Node parent;
   public NodeToken nodeToken;
   public TableReference tableReference;
   public NodeOptional nodeOptional;
   public NodeToken nodeToken1;
   public ColumnValues columnValues;
   public NodeOptional nodeOptional1;

   public UpdateStatement(NodeToken n0, TableReference n1, NodeOptional n2, NodeToken n3, ColumnValues n4, NodeOptional n5) {
      nodeToken = n0;
      if ( nodeToken != null ) nodeToken.setParent(this);
      tableReference = n1;
      if ( tableReference != null ) tableReference.setParent(this);
      nodeOptional = n2;
      if ( nodeOptional != null ) nodeOptional.setParent(this);
      nodeToken1 = n3;
      if ( nodeToken1 != null ) nodeToken1.setParent(this);
      columnValues = n4;
      if ( columnValues != null ) columnValues.setParent(this);
      nodeOptional1 = n5;
      if ( nodeOptional1 != null ) nodeOptional1.setParent(this);
   }

   public UpdateStatement() {}

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

