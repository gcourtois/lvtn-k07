//
// Generated by JTB 1.3.2
//

package com.res.cobol.syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * nodeToken -> &lt;K_INSERT&gt;
 * nodeToken1 -> &lt;K_INTO&gt;
 * tableReference -> TableReference()
 * nodeOptional -> [ &lt;LPARENCHAR&gt; TableColumn() ( &lt;COMMACHAR&gt; TableColumn() )* &lt;RPARENCHAR&gt; ]
 * nodeChoice -> ( &lt;K_VALUES&gt; &lt;LPARENCHAR&gt; PlSqlExpressionList() &lt;RPARENCHAR&gt; | SelectStatement() )
 * </PRE>
 */
public class InsertStatement extends com.res.cobol.RESNode implements Node {
   private Node parent;
   public NodeToken nodeToken;
   public NodeToken nodeToken1;
   public TableReference tableReference;
   public NodeOptional nodeOptional;
   public NodeChoice nodeChoice;

   public InsertStatement(NodeToken n0, NodeToken n1, TableReference n2, NodeOptional n3, NodeChoice n4) {
      nodeToken = n0;
      if ( nodeToken != null ) nodeToken.setParent(this);
      nodeToken1 = n1;
      if ( nodeToken1 != null ) nodeToken1.setParent(this);
      tableReference = n2;
      if ( tableReference != null ) tableReference.setParent(this);
      nodeOptional = n3;
      if ( nodeOptional != null ) nodeOptional.setParent(this);
      nodeChoice = n4;
      if ( nodeChoice != null ) nodeChoice.setParent(this);
   }

   public InsertStatement() {}

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

